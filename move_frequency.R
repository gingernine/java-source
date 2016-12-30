#モデルによる期待値と実際の値で，変動の頻度を比較する．

library(SimpsonRule)

factorial <- function(n) {
    if (n==0) {
        1
    } else if (n > 0) {
        prod(n)
    }
}

bessel <- function(x, n) {
    sum <- 1
    diff <- 1
    i <- 1
    while (diff >= 1e-08) {
        diff <- 1
        for (j in 1:i) {
            diff <- diff * (x*x/4) / (j*(n+j))
        }
        sum <- sum + diff
        i <- i+1
    }
    for (j in 0:n-1) {
        sum <- sum / (n-j)
    }
    return((x/2)^n * sum)
}

f_A <- function(t, r_A, l_A, m_A) {
    exp(-(l_A+m_A)*t) * r_A/t * (m_A/l_A)^(r_A/2) * besselI(2*t*sqrt(l_A*m_A), r_A)
}

f_B <- function(t, r_B, l_B, m_B) {
    exp(-(l_B+m_B)*t) * r_B/t * (m_B/l_B)^(r_B/2) * besselI(2*t*sqrt(l_B*m_B), r_B)
}

f_A_Exp <- function(t, r_A, l_A, m_A) {
    exp(-(l_A+m_A)*t) * r_A * (m_A/l_A)^(r_A/2) * besselI(2*t*sqrt(l_A*m_A), r_A)
}

f_B_Exp <- function(t, r_B, l_B, m_B) {
    exp(-(l_B+m_B)*t) * r_B * (m_B/l_B)^(r_B/2) * besselI(2*t*sqrt(l_B*m_B), r_B)
}

f_U <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_A(t, r_A, l_A, m_A) * (1 - integrate(f_B, 0, t, r_B, l_B, m_B)$value)
}

f_D <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_B(t, r_B, l_B, m_B) * (1 - integrate(f_A, 0, t, r_A, l_A, m_A)$value)
}

f_U_Exp <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_A_Exp(t, r_A, l_A, m_A) * ( 1 - integrate(f_B, 0, t, r_B, l_B, m_B)$value )
}

f_D_Exp <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_B_Exp(t, r_B, l_B, m_B) * ( 1 - integrate(f_A, 0, t, r_A, l_A, m_A)$value )
}

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output\\statistics_of_the_limit_order_book"
datayear <- "\\2007"

#パラメータ集合の行列 parameters を作成する．
subdir <- "\\yearly"
filepath <- paste(maindir, subdir, datayear, "_.csv", sep="", collapse=NULL)
data <- read.csv(filepath, sep=",", header=T)
parameters <- cbind(data["Averege.Pieces.of.One.Market.Buy.Order"],
                    data["Averege.Pieces.of.One.Market.sell.Order"],
                    data["Averege.Pieces.of.One.limit.Buy.Order"],
                    data["Averege.Pieces.of.One.limit.sell.Order"])
tmp <- ""
for (r in 1:length(data[, "date"])) {
    if (tmp != data[r, "date"]) {
        rownames(parameters)[r] <- paste(substring(data[r, "date"], 1, 4), substring(data[r, "date"], 5, 6), substring(data[r, "date"], 7, 8),
                                         "morning", sep="/", collapse=NULL)
    } else {
        rownames(parameters)[r] <- paste(substring(data[r, "date"], 1, 4), substring(data[r, "date"], 5, 6), substring(data[r, "date"], 7, 8),
                                         "afternoon", sep="/", collapse=NULL)
    }
    tmp <- data[r, "date"]
}

#初期デプス r を取得する．
subdir <- "\\initial_depth"
up_down <- c( "\\after_up", "\\after_down" )
sessioins <- c( "\\morning", "\\afternoon" )

for (ud in up_down) {
    depthmat <- matrix(0, ncol=2, nrow=nrow(parameters))
    for (d in 1:nrow(data["date"])) {
        for (session in sessions) {
            filepath <- paste(maindir, subdir, datayear, ud, session, "\\", data[d, "date"], "_.csv", sep="", collapse=NULL)
            
            if (file.exists(filepath)) {
                depth <- read.csv(filepath, sep=",", header=F)
                depthmat[d, 1] <- mean(depth[, 7]) # best bid
                depthmat[d, 2] <- mean(depth[, 9]) # best ask
            }
        }
    }
    if (ud == "\\after_up") {
        UD <- "r^U"
    } else {
        UD <- "r^D"
    }
    colnames(depthmat) <- c( paste(UD, "_B", sep="", collapse=NULL), paste(UD, "_A", sep="", collapse=NULL) )
    parameters <- cbind(parameters, depthmat)
}

#到着率を取得する．
unit <- 30
subdir <- "\\arrival_time_series"
filepath <- paste(maindir, subdir, datayear, "\\arrival_rate_per_", unit, "pieces.csv", sep="", collapse=NULL)
ratemat <- read.csv(filepath, header=T)
parameters <- cbind(parameters, ratemat)

subdir <- "\\move_frequency"
filepath <- paste(maindir, subdir, datayear, "_.csv", sep="", collapse=NULL)
move_freq <- read.csv(filepath, header=T)
move_freq <- cbind(move_freq, matrix(0, nrow=nrow(move_freq), ncol=2))

integral <- function(f, lower, upper, ...) {
    error <- ""
    res <- 0
    while (class(error)!="try-error"){
        tmp <- res
        error <- try(res <- integrate(f, lower, upper, ...)$value)
        upper <- upper + 10
    }
    return(tmp)
}

probmat <- matrix(0, nrow=nrow(parameters), ncol=4)
for (r in 1:nrow(parameters)) {
    r_U_A <- ceiling(parameters[r, "r^U_A"] / unit)
    r_D_A <- ceiling(parameters[r, "r^D_A"] / unit)
    r_U_B <- ceiling(parameters[r, "r^U_B"] / unit)
    r_D_B <- ceiling(parameters[r, "r^D_B"] / unit)
    l_A <- parameters[r, "lambda_A"]
    l_B <- parameters[r, "lambda_B"]
    m_A <- parameters[r, "mu_A"]
    m_B <- parameters[r, "mu_B"]
    p_UU <- integral(f_U, 0, 100, r_U_A, l_A, m_A, r_U_B, l_B, m_B)
    p_UD <- integral(f_D, 0, 100, r_U_A, l_A, m_A, r_U_B, l_B, m_B)
    p_DU <- integral(f_U, 0, 100, r_D_A, l_A, m_A, r_D_B, l_B, m_B)
    p_DD <- integral(f_D, 0, 100, r_D_A, l_A, m_A, r_D_B, l_B, m_B)
    probmat[r,] <- c(p_UU, p_UD, p_DU, p_DD)
}
colnames(probmat) <- c( "p_UU", "p_UD", "p_DU", "p_DD" )
rownames(probmat) <- rownames(parameters)

wfiledir <- paste(maindir, "\\transition_probability", datayear, sep="", collapse=NULL)
if (!file.exists(wfiledir)) {
    dir.create(wfiledir, recursive=T)
}
wfilepath <- paste(wfiledir, "\\arrival_rate_per_", unit, "pieces_Not_stochastic_initial_depth.csv", sep="", collapse=NULL)
write.csv(probmat, wfilepath, quote=F)

# transient_prob summary
mean_UU <- mean(probmat[,1])
sd_UU <- sd(probmat[,1])
mean_UD <- mean(probmat[,2])
sd_UD <- sd(probmat[,2])
mean_DU <- mean(probmat[,3])
sd_DU <- sd(probmat[,3])
mean_DD <- mean(probmat[,4])
sd_DD <- sd(probmat[,4])
print(paste("{", mean_UU, "\\(", sd_UU, ")} & {", mean_UD, "\\(", sd_UD, ")}", sep="", collapse=NULL))
print(paste("{", mean_DU, "\\(", sd_DU, ")} & {", mean_DD, "\\(", sd_DD, ")}", sep="", collapse=NULL))
