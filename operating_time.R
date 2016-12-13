#稼働時間の記述統計量を計算


maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\operating_time\\"
bidask <- c( "\\bid", "\\ask" )
datayear <- "\\2007"
sessions <- c( "\\morning", "\\afternoon" )

sum <- matrix(0, ncol=2, nrow=2)
rownames(sum) <- c( "Mean", "S.D." )
colnames(sum) <- c( "\\T_A", "\\T_B" )
i <- 1

for (ba in bidask) {
    summary <- matrix(0, nrow=1, ncol=7)
    for (session in sessions) {
        datadir <- paste(maindir, currentdir, ba, datayear, session, sep="", collapse=NULL)
        for (name in list.files(datadir)) {
            filepath <- paste(datadir, "\\", name, sep="", collapse=NULL)
            data <- read.csv(filepath, sep=",", header=F)
            
            vec <- matrix(0, nrow=1, ncol=7)
            rownames(vec) <- substring(name, 1, 8)
            colnames(vec) <- c("Mean", "S.D.", "Median", "Kurtosis", "Skewness", "Minimum", "Maximum")
            Mean <- mean(data[,1], na.rm=T)
            SD <- sd(data[,1], na.rm=T)
            Median <- median(data[,1], na.rm=T)
            Kurtosis <- mean((vec - Mean)^4)/(SD^4)
            Skewness <- mean((vec - Mean)^3)/(SD^3)
            vec[1,] <- c(Mean, SD, Median, Kurtosis, Skewness, min(data[,1], na.rm=T), max(data[,1], na.rm=T))
            summary <- rbind(summary, vec)
        }
    }
    
    for (rname in colnames(sum)) {
        sum["Mean", i] <- mean(summary[,1])
        sum["S.D.", i] <- sd(summary[,1])
    }
    i <- 2
    
    #tex code
    code <- ""
    for (cname in colnames(sum)) {
        code <- paste(code, " & ", "\\", cname, sep="", collapse=NULL)
    }
    code <- paste(code, " \\ \\hline", sep="", collapse=NULL)
    code <- matrix(code, ncol=1, nrow=1)
    
    for (rname in rownames(sum)) {
        line <- paste("{\\", "rm ", rname, "} & ", sep="", collapse=NULL)
        for (c in seq(ncol(sum))) {
            if (c == ncol(sum)) {
                line <- paste(line, "$", sum[rname, c], "$", " \\ \\hline", sep="", collapse=NULL)
            } else {
                line <- paste(line, "$", sum[rname, c], "$", " & ", sep="", collapse=NULL)
            }
        }
        code <- rbind(code, matrix(line, nrow=1, ncol=1))
    }
}

wfiledir <- paste(maindir, currentdir, "\\summary", sep="", collapse=NULL)
if (!file.exists(wfiledir)) {
    dir.create(wfiledir)
}

wfilepath <- paste(maindir, currentdir, "\\summary", datayear, "_texcode.csv", sep="", collapse=NULL)
write.csv(code, wfilepath, quote = F, row.names = T)
