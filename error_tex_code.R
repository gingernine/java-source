#エラーデータを統合，tex codeにする．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output\\"
yearvec <- c( "2006", "2007", "2008", "2009", "2010", "2011",
              "2012", "2013", "2014", "2015", "2016" ) 
errordirs <- c( "\\bid_or_ask_is_na", "\\bid_equal_to_ask" )

for (currentdir in errordirs) {
    sum <- matrix(0, nrow=1, ncol=9)
    
    for (datayear in yearvec) {
        datadir <- paste(maindir, datayear, currentdir, "_in_rawcsv_2\\daily", sep="", collapse=NULL)
        fnames <- dir(path=datadir, pattern=".csv", all.files=T)
        if (length(fnames) != 0) {
            for (name in fnames) {
                filepath <- paste(maindir, datayear, currentdir, "_in_rawcsv_2\\daily\\", name, sep="", collapse=NULL)
                data <- read.csv(filepath, sep=",", header=F)
                for (i in seq(nrow(data))) {
                    date <- data[i,1]
                    data[i,1] <- paste(substring(date, 1, 4), substring(date, 5, 6), substring(date, 7, 8), sep="/", collapse=NULL)
                    sum <- rbind(sum, data[i,1:9])
                }
                print(filepath)
            }
        }
    }
    
    sum <- sum[-1,-3]
    #tex code
    code <- ""
    for (rowname in c("date", "time", "", "", "best bid price", "best bid depth", "best ask price", "best ask depth")) {
        code <- paste(code, " & ", "{\\", "rm ", rowname, "}", sep="", collapse=NULL)
    }
    code <- paste(code, " \\ \\hline", sep="", collapse=NULL)
    code <- matrix(code, ncol=1, nrow=1)
    
    for (r in seq(nrow(sum))) {
        line <- paste("\t\t\t\t\t$", sum[r, 1], "$ & ", sep="", collapse=NULL)
        for (c in seq(ncol(sum))[-1]) {
            if (c == ncol(sum)) {
                line <- paste(line, "$", sum[r, c], "$", " \\ \\hline", sep="", collapse=NULL)
            } else {
                line <- paste(line, "$", sum[r, c], "$", " & ", sep="", collapse=NULL)
            }
        }
        code <- rbind(code, matrix(line, nrow=1, ncol=1))
    }
    
    wfiledir <- paste(maindir, "statistics_of_the_limit_order_book\\error", sep="", collapse=NULL)
    if (!file.exists(wfiledir)) {
        dir.create(wfiledir)
    }
    
    wfilepath <- paste(wfiledir, currentdir, "_texcode.csv", sep="", collapse=NULL)
    write.csv(code, wfilepath, quote = F, row.names = T)
}
