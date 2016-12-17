#モデルによる期待値と実際の値で，変動の頻度を比較する．

f_A <- function(t, r_A, l_A, m_A) {
    exp(-(l_A+m_A)*t) * r_A/t * (m_A/l_A)^(r_A/2) * besselI(2*t*sqrt(l_A*m_A), r_A)
}

f_B <- function(t, r_B, l_B, m_B) {
    exp(-(l_B+m_B)*t) * r_B/t * (m_B/l_B)^(r_B/2) * besselI(2*t*sqrt(l_B*m_B), r_B)
}

f_A_Exp <- function(t, r_A, l_A, m_A) {
    t * f_A(t, r_A, l_A, m_A)
}

f_B_Exp <- function(t, r_B, l_B, m_B) {
    t * f_B(t, r_B, l_B, m_B)
}

f_U <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_A(t, r_A, l_A, m_A) - f_A(t, r_A, l_A, m_A) * integrate(f_B, 0, t, r_B, l_B, m_B)$value
}

f_D <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_B(t, r_B, l_B, m_B) - f_B(t, r_B, l_B, m_B) * integrate(f_A, 0, t, r_A, l_A, m_A)$value
}

f_U_Exp <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    t * f_U(t, r_A, l_A, m_A, r_B, l_B, m_B)
}

f_D_Exp <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    t * f_D(t, r_A, l_A, m_A, r_B, l_B, m_B)
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
subdir <- "\\time_interval"
dirlist <- c( "\\time_interval_limit_buy", "\\time_interval_limit_sell", "\\time_interval_market_buy", "\\time_interval_market_sell" )
ratemat <- matrix(0, ncol=4, nrow=nrow(parameters))
colnames(ratemat) <- dirlist
for (dir in dirlist){
    filepath <- paste(maindir, subdir, dir, datayear, "\\statistics_summary\\morning", ".csv", sep="", collapse=NULL)
    rate1 <- read.csv(filepath, sep=",", header=T)
    filepath <- paste(maindir, subdir, dir, datayear, "\\statistics_summary\\afternoon", ".csv", sep="", collapse=NULL)
    rate2 <- read.csv(filepath, sep=",", header=T)
    r1 <- 1
    r2 <- 1
    boolean <- T
    for (r in 1:nrow(ratemat)) {
        if (r == 1) {
            ratemat[r, dir] <- 1/rate1[r1, "Mean"]
            r1 <- r1 + 1
            next
        }
        if (boolean) {
            ratemat[r, dir] <- 1/rate1[r1, "Mean"]
            r1 <- r1 + 1
            boolean <- F
        } else {
            ratemat[r, dir] <- 1/rate2[r2, "Mean"]
            r2 <- r2 + 1
            boolean <- T
        }
    }
}
colnames(ratemat) <- c( "lambda_B", "lambda_A", "mu_A", "mu_B" )
parameters <- cbind(parameters, ratemat)

subdir <- "\\move_frequency"
filepath <- paste(maindir, subdir, datayear, "_.csv", sep="", collapse=NULL)
move_freq <- read.csv(filepath, header=T)
move_freq <- cbind(move_freq, matrix(0, nrow=nrow(move_freq), ncol=2))
integral_interval <- c(0, 100)
for (r in 1:nrow(parameters)) {
    meanvol <-  (parameters[r, "Averege.Pieces.of.One.Market.Buy.Order"] + parameters[r, "Averege.Pieces.of.One.limit.sell.Order"]) / 2
    r_U_A <- parameters[r, "r^U_A"] / meanvol
    r_D_A <- parameters[r, "r^D_A"] / meanvol
    meanvol <-  (parameters[r, "Averege.Pieces.of.One.Market.sell.Order"] + parameters[r, "Averege.Pieces.of.One.limit.Buy.Order"]) / 2
    r_U_B <- parameters[r, "r^U_B"] / meanvol
    r_D_B <- parameters[r, "r^D_B"] / meanvol
    l_A <- parameters[r, "lambda_A"]
    l_B <- parameters[r, "lambda_B"]
    m_A <- parameters[r, "mu_A"]
    m_B <- parameters[r, "mu_B"]
    E_U <- integrate(f_U_Exp, integral_interval[1], integral_interval[2], r_U_A, l_A, m_A, r_U_B, l_B, m_B)$value + integrate(f_D_Exp, integral_interval[1], integral_interval[2], r_U_A, l_A, m_A, r_U_B, l_B, m_B)$value
    E_D <- integrate(f_U_Exp, integral_interval[1], integral_interval[2], r_D_A, l_A, m_A, r_D_B, l_B, m_B)$value + integrate(f_D_Exp, integral_interval[1], integral_interval[2], r_D_A, l_A, m_A, r_D_B, l_B, m_B)$value
    move_freq[r, 3] <- 1 / move_freq[r, 2]
    move_freq[r, 4] <- 2 / (E_U + E_D)
}

