#到着時間間隔とサービス時間間隔の分布の検定を行う．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\time_interval\\"
datayear <- "\\2007"
branchs <- c( "time_interval_limit_buy", "time_interval_limit_sell", "time_interval_market_buy", "time_interval_market_sell" )
sessions <- c( "\\morning", "\\afternoon" )

#はじめに時間間隔のテーブルを作成する．10秒以上はプールする．
intervals <- c( "[0-1)", "[1-2)", "[2-3)", "[3-4)", "[4-5)", 
                "[5-6)", "[6-7)", "[7-8)", "[8-9)", "[9-10)","[10-infty)", 
                "statistic_exp", "statistic_gam", "statistic_pa2", "statistic_pa3", "statistic_pa4", "chi-square",
                "adopted_exp", "adopted_gam", "adopted_pa2", "adopted_pa3", "adopted_pa4" )

#                "[10-11)", "[11-12)", "[12-13)", "[13-14)", "[14-15)",
#                "[15-16)", "[16-17)", "[17-18)", "[18-19)", "[19-20)", "[20-infty)" )
colnum <- length(intervals)
intervalnum <- colnum - 11

for (branch in branchs) {
    for (session in sessions) {
        dirpath <- paste(maindir, currentdir, branch, datayear, session, sep = "", collapse = NULL)
        table <- matrix(0, nrow = 1, ncol = colnum)
        for (name in list.files(dirpath)) {
            filename <- paste(maindir, currentdir, branch, datayear, session, "\\", name, sep = "", collapse = NULL)
            error <- try(read.csv(filename, sep=",", header = T))
            if (class(error)=="try-error"){
                next
            }
            data <- read.csv(filename, sep=",", header = T)
            num <- length(data[, 1])
            date <- substring(name, 1, 8)
            vector <- matrix(0, nrow = 1, ncol = colnum)
            rownames(vector) <- date
            colnames(vector) <- intervals
            maxsec <- intervalnum - 1
            for (n in data[,1]) {
                if (n >= maxsec) {
                    vector[1,maxsec+1] <- vector[1,maxsec+1] + 1
                    next
                }
                for (i in 1:maxsec) {
                    if(n == i-1) {
                        #例えばデータが3秒としていたら，これは[3,4)秒の間に来たと考える
                        vector[1,i] <- vector[1,i] + 1
                        break
                    }
                }
            }
            
            #カイ二乗検定統計量を計算する
            #指数分布
            dense_exp <- function(x, param) {
                param[1] * exp(-param[1]*x)
            }
            log_dense_exp <- function(x,param) {
                n <- length(x)
                return(n * log(param[1]) - param[1] * sum(x))
            }
            #Gamma分布
            dense_gam <- function(x, param) {
                1/(gamma(param[1])*param[2]^param[1]) * x^(param[1]-1) * exp(-x/param[2])
            }
            log_dense_gam <- function(x, param) {
                n <- length(x)
                return(-n*log(gamma(param[1])) - n*param[1]*log(param[2]) + (param[1]-1)*prod(x) - sum(x)/param[2])
            }
            #Pareto分布Ⅱ
            dense_pa2 <- function(x, param) {
                param[1]*param[2]^param[1]/(x + param[2])^(1 + param[1])
            }
            log_dense_pa2 <- function(x, param) {
                n <- length(x)
                return(n*log(param[1]) + n*param[1]*log(param[2]) - (1+param[1])*sum(log(x+param[2])))
            }
            #Pareto分布Ⅲ
            dense_pa3 <- function(x, param) {
                x^(-1+1/param[2])*param[1]^(-1/param[2]) / param[2] / (1 + (param[1]/x)^(-1/param[2]))^2
            }
            log_dense_pa3 <- function(x, param) {
                n <- length(x)
                return((-1+1/param[2])*log(prod(x)) - n/param[2]*log(param[1]) - n*log(param[2]) - 2*sum(log(1 + (param[1]/x)^(-1/param[2]))))
            }
            #Pareto分布Ⅳ
            dense_pa4 <- function(x, param) {
                x^(-1+1/param[3])*param[1]^(-1/param[3])*param[2] / param[3] / (1 + (param[1]/x)^(-1/param[3]))^(1+param[2])
            }
            log_dense_pa4 <- function(x, param) {
                n <- length(x)
                return((-1+1/param[3])*log(prod(x)) - n/param[3]*log(param[1]) + log(param[2]) - n*log(param[3]) - (1+param[2])*sum(log(1 + (param[1]/x)^(-1/param[3]))))
            }
            Likelihood <- function(x,log_dense) {
                #尤度関数を返す．
                return(function(par) {
                    log_dense(x,par)
                })
            }
            find_param <- function(log_dense) {
                c <- 1
                while (c != 0){
                    par1 <- runif(1,0.001,1000)
                    par2 <- runif(1,0.001,1000)
                    par3 <- runif(1,0.001,1000)
                    nonzero <- 1:length(data[,1])
                    for (n in 1:length(data[,1])) {
                        if (data[n,1]==0) {
                            nonzero[n] = runif(1,0.001,0.999)
                        } else {
                            nonzero[n] = data[n,1]
                        }
                    }
                    
                    e <- try (opt <- optim(par=c(par1,par2,par3), fn=Likelihood(nonzero, log_dense), control=list(fnscale=-1)))
                    if (class(e) == "try-error") {
                        c <- 1
                        next
                    } else {
                        c <- opt$convergence
                    }
                }
                return(opt$par)
            }
            
            statistic_exp <- 0 #検定統計量Exponential
            statistic_gam <- 0 #検定統計量Gamma
            statistic_wei <- 0 #検定統計量Weibull
            statistic_pa2 <- 0 #検定統計量Parete2
            statistic_pa3 <- 0 #検定統計量Pareto3
            statistic_pa4 <- 0 #検定統計量Pareto4
            DF <- intervalnum - 2
            for (i in 1:maxsec) {
                theo_exp <- num * integrate(dense_exp, i-1, i, find_param(log_dense_exp))$value
                #theo_gam <- num * integrate(dense_gam, i-1, i, find_param(log_dense_gam))$value
                #theo_pa2 <- num * integrate(dense_pa2, i-1, i, find_param(log_dense_pa2))$value
                #theo_pa3 <- num * integrate(dense_pa3, i-1, i, find_param(log_dense_pa3))$value
                #theo_pa4 <- num * integrate(dense_pa4, i-1, i, find_param(log_dense_pa4))$value
                realized <- vector[1,i]
                statistic_exp <- statistic_exp + (theo_exp - realized) * (theo_exp - realized) / theo_exp
                #statistic_gam <- statistic_gam + (theo_gam - realized) * (theo_gam - realized) / theo_gam
                #statistic_pa2 <- statistic_pa2 + (theo_pa2 - realized) * (theo_pa2 - realized) / theo_pa2
                #statistic_pa3 <- statistic_pa3 + (theo_pa3 - realized) * (theo_pa3 - realized) / theo_pa3
                #statistic_pa4 <- statistic_pa4 + (theo_pa4 - realized) * (theo_pa4 - realized) / theo_pa4
            }
            theo_exp <- num * (1 - integrate(dense_exp, 0, maxsec, find_param(log_dense_exp))$value)
            #theo_gam <- num * (1 - integrate(dense_gam, 0, maxsec, find_param(log_dense_gam))$value)
            #theo_pa2 <- num * (1 - integrate(dense_pa2, 0, maxsec, find_param(log_dense_pa2))$value)
            #theo_pa3 <- num * (1 - integrate(dense_pa3, 0, maxsec, find_param(log_dense_pa3))$value)
            #theo_pa4 <- num * (1 - integrate(dense_pa4, 0, maxsec, find_param(log_dense_pa4))$value)
            realized <- vector[1,maxsec+1]
            statistic_exp <- statistic_exp + (theo_exp - realized) * (theo_exp - realized) / theo_exp
            #statistic_gam <- statistic_gam + (theo_gam - realized) * (theo_gam - realized) / theo_gam
            #statistic_pa2 <- statistic_pa2 + (theo_pa2 - realized) * (theo_pa2 - realized) / theo_pa2
            #statistic_pa3 <- statistic_pa3 + (theo_pa3 - realized) * (theo_pa3 - realized) / theo_pa3
            #statistic_pa4 <- statistic_pa4 + (theo_pa4 - realized) * (theo_pa4 - realized) / theo_pa4
            vector[1,intervalnum+1] <- statistic_exp
            vector[1,intervalnum+2] <- statistic_gam
            vector[1,intervalnum+3] <- statistic_pa2
            vector[1,intervalnum+4] <- statistic_pa3
            vector[1,intervalnum+5] <- statistic_pa4
            vector[1,intervalnum+6] <- qchisq(df=DF, 0.95)
            if (statistic_exp < qchisq(df=DF, 0.95)) {
                vector[1,intervalnum+7] <- "adopted"
            }
            if (statistic_gam < qchisq(df=DF, 0.95)) {
                vector[1,intervalnum+8] <- "adopted"
            }
            if (statistic_pa2 < qchisq(df=DF, 0.95)) {
                vector[1,intervalnum+9] <- "adopted"
            }
            if (statistic_pa3 < qchisq(df=DF, 0.95)) {
                vector[1,intervalnum+10] <- "adopted"
            }
            #if (statistic_pa4 < qchisq(df=DF, 0.95)) {
            #    vector[1,intervalnum+11] <- "adopted"
            #}
            table <- rbind(table, vector)
        }
        wfiledir <- paste(maindir, currentdir, branch, datayear, "\\table", sep = "", collapse = NULL)
        if (!file.exists(wfiledir)){
            dir.create(wfiledir)
        }
        wfilename <- paste(maindir, currentdir, branch, datayear, "\\table", session, ".csv", sep = "", collapse = NULL)
        write.csv(table[-1,], wfilename, quote = F, row.names = T)
    }
}

