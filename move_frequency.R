#モデルによる期待値と実際の値で，変動の頻度を比較する．

besseli0 <- function(x, e) {
    ax <- abs(x)
    if (ax < 3.75) {
        y <- x / 3.75
        y <- y * y
        ans <- 1.0 + y*(3.5156229 + y*(3.0899424 + y*(1.2067492
                    + y*(0.2659732 + y*(0.360768e-1 + y*0.45813e-2)))))
        ans <- ans * exp(-e)
    } else {
        y <- 3.75 / ax
        ans <- (exp(ax-e) / sqrt(ax)) * (0.39894228 + y*(0.1328592e-1
                    + y*(0.225319e-2 + y*(-0.157565e-2 + y*(0.916281e-2
                    + y*(-0.2057706e-1 + y*(0.2635537e-1 + y*(-0.1647633e-1
                    + y*0.392377e-2))))))))
    }
    return(ans)
}

besseli1 <- function(x, e) {
    ax <- abs(x)
    if(ax < 3.75) {
        y <- x / 3.75
        y <- y * y
        ans <- ax*(0.5+y*(0.87890594+y*(0.51498869+y*(0.15084934
                    +y*(0.2658733e-1+y*(0.301532e-2+y*0.32411e-3))))))
        ans <- ans * exp(-e)
    } else {
        y <- 3.75 / ax
        ans <- 0.2282967e-1+y*(-0.2895312e-1+y*(0.1787654e-1
                                -y*0.420059e-2))
        ans <- 0.39894228+y*(-0.3988024e-1+y*(-0.362018e-2
                                             +y*(0.163801e-2+y*(-0.1031555e-1+y*ans))))
        ans <- ans * (exp(ax-e)/sqrt(ax))
    }
    return(ans)
}

besseli <- function(x, n, e) {
    ACC <- 200
    if (n == 0) {
        return(besseli0(x, e))
    } else if (n == 1) {
        return(besseli1(x, e))
    } else {
        tox <- 2 / abs(x)
        bi <- 1
        bip <- 0
        j=2*(n+floor(sqrt(ACC*n)))
        while(j > 0) {
            bim <- bip + j * tox * bi
            bip <- bi
            bi <- bim
            if (j == n) {
                ans <- bip
            }
            j <- j-1
        }
        ans <- ans * besseli0(x, e) / bi
        return(ans)
    }
}

f_A <- function(t, r_A, l_A, m_A) {
    r_A/t * (m_A/l_A)^(r_A/2) * besseli(2*t*sqrt(l_A*m_A), r_A, (l_A+m_A)*t)
}

f_B <- function(t, r_B, l_B, m_B) {
    r_B/t * (m_B/l_B)^(r_B/2) * besseli(2*t*sqrt(l_B*m_B), r_B, (l_B+m_B)*t)
}

f_A_Exp <- function(t, r_A, l_A, m_A) {
    r_A * (m_A/l_A)^(r_A/2) * besseli(2*t*sqrt(l_A*m_A), r_A, (l_A+m_A)*t)
}

f_B_Exp <- function(t, r_B, l_B, m_B) {
    r_B * (m_B/l_B)^(r_B/2) * besseli(2*t*sqrt(l_B*m_B), r_B, (l_B+m_B)*t)
}

f_U <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_A(t, r_A, l_A, m_A) * integrate(f_B, 1e-10, t, r_B, l_B, m_B)$value
}

f_D <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_B(t, r_B, l_B, m_B) * integrate(f_A, 1e-10, t, r_A, l_A, m_A)$value
}

f_U_Exp <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_A_Exp(t, r_A, l_A, m_A) * integrate(f_B, 1e-10, t, r_B, l_B, m_B)$value
}

f_D_Exp <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_B_Exp(t, r_B, l_B, m_B) * integrate(f_A, 1e-10, t, r_A, l_A, m_A)$value
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
sessions <- c( "\\morning", "\\afternoon" )

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
unit <- 1
subdir <- "\\arrival_time_series"
filepath <- paste(maindir, subdir, datayear, "\\arrival_rate_per_", unit, "pieces.csv", sep="", collapse=NULL)
ratemat <- read.csv(filepath, header=T)
parameters <- cbind(parameters, ratemat)

wfilepath <- "C:\\Users\\kklab\\Desktop\\yurispace\\integration_cpp\\source"
wfilename <- paste(datayear, "\\parameters_", unit, "pieces.csv", sep="", collapse=NULL)
write.csv(parameters, paste(wfilepath, wfilename, sep="", collapse=NULL))

subdir <- "\\move_frequency"
filepath <- paste(maindir, subdir, datayear, "_.csv", sep="", collapse=NULL)
move_freq <- read.csv(filepath, header=T)
move_freq <- cbind(move_freq, matrix(0, nrow=nrow(move_freq), ncol=2))

integral <- function(f, lower, upper, r_A, l_A, m_A, r_B, l_B, m_B, up) {
    if (up) {
        if (l_A < m_A)
            p <- 1
        else
            p <- (l_A/m_A)^{-r_A}
    } else {
        if (l_B < m_B)
            p <- 1
        else
            p <- (l_B/m_B)^{-r_B}
    }
    res <- p - integrate(f, lower, upper, r_A, l_A, m_A, r_B, l_B, m_B)$value
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
    print(rownames(parameters)[r])
    p_UU <- integrate(f_U, 1e-10, 100, r_U_A, l_A, m_A, r_U_B, l_B, m_B)
    curve(f_U(x, r_U_A, l_A, m_A, r_U_B, l_B, m_B, T), xlim=c(10,1000))
    #p_UD <- integrate(f_D, 1e-10, 100, r_U_A, l_A, m_A, r_U_B, l_B, m_B)
    curve(f_D(x, r_U_A, l_A, m_A, r_U_B, l_B, m_B, F), xlim=c(10,1000))
    #p_DU <- integrate(f_U, 1e-10, 100, r_D_A, l_A, m_A, r_D_B, l_B, m_B)
    curve(f_U(x, r_D_A, l_A, m_A, r_D_B, l_B, m_B, T), xlim=c(10,1000))
    #p_DD <- integrate(f_D, 1e-10, 100, r_D_A, l_A, m_A, r_D_B, l_B, m_B)
    curve(f_D(x, r_D_A, l_A, m_A, r_D_B, l_B, m_B, F), xlim=c(10,1000))
    probmat[r,] <- c(p_UU, p_UD, p_DU, p_DD)
}
colnames(probmat) <- c( "p_UU", "p_UD", "p_DU", "p_DD" )
rownames(probmat) <- rownames(parameters)

wfiledir <- paste(maindir, "\\transition_probability", datayear, sep="", collapse=NULL)
if (!file.exists(wfiledir)) {
    dir.create(wfiledir, recursive=T)
}
wfilepath <- paste(wfiledir, "\\arrival_rate_per_", unit, "pieces_Not_stochastic_initial_depth.csv", sep="", collapse=NULL)
#write.csv(probmat, wfilepath, quote=F)

# transient_prob summary
mean_UU <- mean(probmat[,"p_UU"])
sd_UU <- sd(probmat[,"p_UU"])
mean_UD <- mean(probmat[,"p_UD"])
sd_UD <- sd(probmat[,"p_UD"])
mean_DU <- mean(probmat[,"p_DU"])
sd_DU <- sd(probmat[,"p_DU"])
mean_DD <- mean(probmat[,"p_DD"])
sd_DD <- sd(probmat[,"p_DD"])
print(paste("\substack{", mean_UU, "\\(", sd_UU, ")} & \substack{", mean_UD, "\\(", sd_UD, ")}", sep="", collapse=NULL))
print(paste("\substack{", mean_DU, "\\(", sd_DU, ")} & \substack{", mean_DD, "\\(", sd_DD, ")}", sep="", collapse=NULL))
