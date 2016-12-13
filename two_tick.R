#ザラバにて最良気配値が2ティック以上離れている時間の統計を計算する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output\\"
yearvec <- c( "2006", "2007", "2008", "2009", "2010", "2011",
              "2012", "2013", "2014", "2015", "2016" ) 
currentdir <- "\\bid_ask_spread_two_tick\\daily"

summary <- matrix(0, nrow=1, ncol=5)

for (datayear in yearvec) {
    dirpath <- paste(maindir, datayear, currentdir, sep = "", collapse = NULL)
    table <- matrix(0, nrow = 1, ncol = 1)
    for (name in list.files(dirpath)) {
        filename <- paste(maindir, datayear, currentdir, "\\", name, sep = "", collapse = NULL)
        error <- try(read.csv(filename, sep=",", header = F))
        if (class(error)=="try-error"){
            #ファイルが空の場合のエラーキャッチ．
            next
        }
        data <- read.csv(filename, sep=",", header = F)
        date <- substring(name, 1, 8)

        denom <- data[1,4] #denominator
        sum <- 0
        for (n in 1:length(data[,1])) {
            if (denom != data[n,4]) {
                vec <- matrix(sum, nrow = 1, ncol = 1)
                rownames(vec) <- paste(substring(date, 1, 4), substring(date, 5, 6), substring(date, 7, 8), sep="/", collapse=NULL)
                colnames(vec) <- "ratio of two tick spread time to the whole continuous time"
                table <- rbind(table, vec)
                denom <- data[n,4]
                sum <- 0
            }
            sum <- sum + data[n,3]/data[n,4]
        }
        vec <- matrix(sum, nrow = 1, ncol = 1)
        rownames(vec) <- paste(substring(date, 1, 4), substring(date, 5, 6), substring(date, 7, 8), sep="/", collapse=NULL)
        colnames(vec) <- "ratio of two tick spread time to the whole continuous time"
        table <- rbind(table, vec)
        
    }
    
    sumvec <- matrix(0, nrow=1, ncol=5)
    rownames(sumvec) <- datayear
    colnames(sumvec) <- c("Mean", "S.D.", "Median", "Minimum", "Maximum")
    Mean <- mean(table[,1], na.rm=T)
    SD <- sd(table[,1], na.rm=T)
    Median <- median(table[,1], na.rm=T)
    sumvec[1,] <- c(Mean, SD, Median, min(table[,1], na.rm=T), max(table[,1], na.rm=T))
    summary <- rbind(summary, sumvec)
    print(date)
    
}
summary <- summary[-1,]
wfiledir <- paste(maindir, "statistics_of_the_limit_order_book\\two_tick_spread", sep = "", collapse = NULL)
if (!file.exists(wfiledir)){
    dir.create(wfiledir)
}
wfilename <- paste(maindir, "statistics_of_the_limit_order_book\\two_tick_spread", "\\summary.csv", sep = "", collapse = NULL)
write.csv(summary, wfilename, quote = F, row.names = T)

#tex code
code <- ""
for (cname in colnames(summary)) {
    code <- paste(code, " & {\\", "rm ", cname, "}", sep="", collapse=NULL)
}
code <- paste(code, " \\\\ \\hline", sep="", collapse=NULL)
code <- matrix(code, ncol=1, nrow=1)

for (rname in rownames(summary)) {
    line <- paste("$", rname, "$ & ", sep="", collapse=NULL)
    for (c in seq(ncol(summary))) {
        if (c == ncol(summary)) {
            line <- paste(line, "$", summary[rname, c], "$", " \\\\ \\hline", sep="", collapse=NULL)
        } else {
            line <- paste(line, "$", summary[rname, c], "$", " & ", sep="", collapse=NULL)
        }
    }
    code <- rbind(code, matrix(line, nrow=1, ncol=1))
}

wfilename <- paste(maindir, "statistics_of_the_limit_order_book\\two_tick_spread", "\\summary_texcode.csv", sep = "", collapse = NULL)
write.csv(code, wfilename, quote = F, row.names = T)

