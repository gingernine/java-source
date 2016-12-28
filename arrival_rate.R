#到着率を時間毎に区切り計算，更には適合度検定する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
subdir <- "\\statistics_of_the_limit_order_book\\arrival_time_series"
datayear <- "\\2007"
branchs <- c( "\\limit_buy", "\\limit_sell", "\\market_buy", "\\market_sell" ) 
sessions <- c( "\\morning", "\\afternoon" )

readcsv <- function(filepath, ...) {
    error <- try(data <- read.csv(filepath, ...))
    if (class(error)=="try-error"){
        return(-1)
    } else {
        return(data)
    }
}

convert_to_seconds <- function(time) {
    # "hhmmss" (時分秒)表示される時間を始点0時0分0秒からの秒で表示する． #
    h <- (time - time %% 10000) * 3600 / 10000
    m <- (time %% 10000 - time %% 100) * 60 / 100
    s <- time %% 100
    return(h + m + s)
}

calc_arrival_rate <- function(time_vector, interval) {
    # interval 分間隔での到着率を計算する． #
    size <- length(time_vector)
    stamp <- convert_to_seconds(time_vector[1]) # time stamp for while loop
    finish <- convert_to_seconds(time_vector[size]) # closing time
    interval <- interval * 60 # time interval in seconds
    p1 <- 2
    p2 <- 1
    retvec <- numeric(ceiling((finish-stamp)/interval))
    while(stamp <= finish) {
        counter <- 0
        while (convert_to_seconds(time_vector[p1]) <= stamp + interval) {
            counter <- counter + 1
            p1 <- p1 + 1
            if (p1 > size) break
        }
        retvec[p2] <- counter / interval
        p2 <- p2 + 1
        stamp <- stamp + interval
    }
    return(retvec)
}
#int10 <- calc_arrival_rate(data[,1], 10)
#plot(int10, type="s", main="", #paste(branch, session, "\\", name, sep="", collapse=NULL), ylim=c(0, 2.0), xlim=c(0, 16))

unit_pieces_arrival <- function(unit, time_pieces) {
    # unit で規定される単位枚数ごとの到着時間の系列を作成する． #
    if(unit == -1) {
        return(time_pieces)
    }
    firstrow <- time_pieces[1,]
    time_pieces <- time_pieces[-1,]
    unit_pieces_series <- matrix(0, ncol=2, nrow=1)
    pieces <- 0
    for (i in 1:nrow(time_pieces)) {
        cur <- time_pieces[i, 2]
        if (cur < unit - pieces) {
            pieces <- pieces + cur
            next
        } else {
            cur <- cur - unit + pieces
            unit_pieces_series <- rbind(unit_pieces_series, matrix(c(time_pieces[i, 1], unit), ncol=2, nrow=1))
            pieces <- 0
        }
        q <- floor(cur / unit)
        pieces <- cur %% unit
        while (q > 0) {
            unit_pieces_series <- rbind(unit_pieces_series, matrix(c(time_pieces[i, 1], unit), ncol=2, nrow=1))
            q <- q-1
        }
    }
    unit_pieces_series[1,] <- matrix(firstrow, ncol=2, nrow=1)
    return(unit_pieces_series)
}

time_interval <- function(time_vector, continuous_time) {
    # 時間間隔の系列を作成する． #
    size <- length(time_vector) - 1
    series <- numeric(size)
    for (i in 1:size) {
        series[i] <- convert_to_seconds(time_vector[i+1]) - convert_to_seconds(time_vector[i])
    }
    return(list(series = series, lambda = size/continuous_time))
}

logarithm_plot <- function(interval_series, lambda, picname) {
    # 時間間隔の系列を受取り，対数変換された累積頻度図を描く． #
    MAX <- max(interval_series)
    freq <- numeric(MAX + 1) #時間間隔は0~1秒 1~2秒 ... MAX~MAX+1秒
    for (i in interval_series) {
        freq[i+1] <- freq[i+1] + 1
    }
    freq <- freq / sum(freq) #頻度の和を1にする．
    size <- length(freq)
    logcum <- numeric(size)
    for (i in 1:size) {
        logcum[i] <- log(sum(freq[i:size]))
    }
    plot(logcum, type="S", main=picname)
    abline(0, -lambda)
}
#logarithm_plot(ret$series, ret$lambda, paste(branch, session, "\\", name, sep="", collapse=NULL))

chi_square_test <- function() {
    # 時間間隔のΧ^2 適合度検定 #
}

system_renewed <- function() {
    # 板の変動毎に時間計測始点を更新したときの時系列を取得． #
    b <- c( "\\time_interval_limit_buy", "\\time_interval_limit_sell", "\\time_interval_market_buy", "\\time_interval_market_sell" )
    for (branch in b) {
        for (session in sessions) {
            dirpath <- paste(maindir, 
                             "\\statistics_of_the_limit_order_book\\time_interval", 
                             branch, datayear, session, sep="", collapse=NULL)
            for (name in list.files(dirpath)) {
                filepath <- paste(dirpath, "\\", name, sep="", collapse=NULL)
                data <- readcsv(filepath, sep=",", header=F)
                if(data == -1) {
                    next
                }
                if (session == "\\morning") {
                    denom <- 7800
                } else {
                    denom <- 9600
                }
                logarithm_plot(data[,1], nrow(data)/denom, paste(branch, session, "\\", name, sep="", collapse=NULL))
            }
        }
    }
}

# main loop
wfilepath <- paste(maindir, subdir, datayear, "\\arrival_rate.csv", sep="", collapse=NULL) #書き出すファイルの指定
table <- matrix(0, ncol=4, nrow=1)
unit <- -1

for (b in 1:4) {
    dirpath1 <- paste(maindir, subdir, datayear, branchs[b], sessions[1], sep="", collapse=NULL)
    dirpath2 <- paste(maindir, subdir, datayear, branchs[b], sessions[2], sep="", collapse=NULL)
    list1 <- list.files(dirpath1) # list of file names(morning)
    list2 <- list.files(dirpath2) # list of file names(afternoon)
    size <- length(list1) + length(list2)
    p1 <- 1
    p2 <- 1
    rate_vec <- numeric(size) # result
    boolean <- T
    for (n in seq(size)) {
        if (n == 1) {
            filepath <- paste(dirpath1, "\\", list1[p1], sep="", collapse=NULL)
            p1 <- p1 + 1
        } else if (boolean) {
            filepath <- paste(dirpath1, "\\", list1[p1], sep="", collapse=NULL)
            p1 <- p1 + 1
            boolean <- F
        } else {
            filepath <- paste(dirpath2, "\\", list2[p2], sep="", collapse=NULL)
            p2 <- p2 + 1
            boolean <- T
        }
        
        data <- readcsv(filepath, sep=",", header=F)
        if (b==1 | b == 2) {
            arg1 = "opening"
            arg2 = ""
            arg3 = ""
        } else {
            arg1 = "opening"
            arg2 = "canceled"
            arg3 = "traded"
        }
        unit_pieces <- unit_pieces_arrival(unit, cbind(data[data$V5==arg1|data$V5==arg2|data$V5==arg3, 1],
                                                       data[data$V5==arg1|data$V5==arg2|data$V5==arg3, 3]))
        ret <- time_interval(unit_pieces[,1], data[1,4])
        rate_vec[n] <- ret$lambda
    }
    
    if (nrow(table) == 1) {
        table <- matrix(0, ncol=4, nrow=size)
    }
    table[,b] <- rate_vec
}

colnames(table) <- c( "lambda_B", "lambda_A", "mu_A", "mu_B" )
write.csv(table, wfilepath, quote = F, row.names = F)

