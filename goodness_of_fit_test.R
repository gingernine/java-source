#適合度検定

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
subdir <- "\\statistics_of_the_limit_order_book\\time_interval"
branchs <- c( "\\time_interval_limit_buy", "\\time_interval_limit_sell", "\\time_interval_market_buy", "\\time_interval_market_sell" ) 
datayear <- "\\2007"
sessions <- c( "\\morning", "\\afternoon" )

factorial <- function(x) {
    if (x == 0) {
        1
    } else {
        prod(seq(x))
    }
}
f_exp <- function(x,l) {
    l * exp(-l*x)
}

#一旦流してグラフ表示するための for loop
for (branch in branchs) {
    for (session in sessions) {
        dirpath <- paste(maindir, subdir, branch, datayear, session, sep="", collapse=NULL)
        for (name in list.files(dirpath) ) {
            filepath <- paste(dirpath, "\\", name, sep="", collapse=NULL)
            error <- try(read.csv(filepath, sep=",", header = F))
            if (class(error)=="try-error"){
                next
            }
            data <- read.csv(filepath, header=F, sep=",")
            
            freq <- matrix(0, ncol=1, nrow=max(data[,1])+1)
            for(i in data[,1]) {
                freq[i+1,1] <- freq[i+1,1] + 1
            }
            
            Exp <- matrix(0, ncol=1,nrow=50)
            lambda <- 1/mean(data[,1])
            for(i in 1:50) {
                Exp[i,1] <- integrate(f_exp, i-1, i, lambda)$value
            }
            
            #2秒まではまとめる．
            freq[2,1] <- freq[1,1] + freq[2,1]
            Exp[2,1] <- Exp[1,1] + Exp[2,1]
            
            barmax <- max( max(freq[,1][-1]), max(sum(freq[,1])*Exp[,1][-1]) )
            
            barplot(freq[,1][-1], xlim=c(0,50), ylim=c(0,barmax))
            par(new=T)
            barplot(sum(freq[,1])*Exp[,1][-1], xlim=c(0,50), ylim=c(0,barmax), col="#b2222220", main=filepath)
            
            #keypressed <- 0
            #while(keypressed == 0) {
            #	print("Press some key.")
            #	keypressed <- readline()
            #}
        }
    }
}

#指数分布の仮定の下で適合度検定を行う．
intervals <- c( "[0-1)", "[1-2)", "[2-3)", "[3-4)", "[4-5)", 
                "[5-6)", "[6-7)", "[7-8)", "[8-9)", "[9-10)",
                "[0-1)", "[1-2)", "[2-3)", "[3-4)", "[4-5)", 
                "[5-6)", "[6-7)", "[7-8)", "[8-9)", "[9-10)","[20-infty)" ) 
tailcols <- c( "statistic_exp", "chi-square", "adopted_exp" )

colnum <- length(intervals)
tailnum <- length(tailcols)

for (branch in branchs) {
    for (session in sessions) {
        dirpath <- paste(maindir, subdir, branch, datayear, session, sep = "", collapse = NULL)
        table <- matrix(0, nrow = 1, ncol = colnum + tailnum)
        for (name in list.files(dirpath)) {
            filename <- paste(dirpath, "\\", name, sep = "", collapse = NULL)
            error <- try(read.csv(filename, sep=",", header = F))
            if (class(error)=="try-error"){
                next
            }
            data <- read.csv(filename, sep=",", header = F)
            
            sumfreq <- length(data[,1])
            date <- paste(substring(name, 1, 4), substring(name, 5, 6), substring(name, 7, 8), sep="/", collapse=NULL)
            vector <- matrix(0, nrow = 1, ncol = colnum)
            rownames(vector) <- date
            colnames(vector) <- intervals
            maxsec <- colnum - 1
            for(i in data[,1]) {
                if (i >= maxsec) {
                    vector[1,colnum] <- vector[1,colnum] + 1
                } else {
                    vector[1,i+1] <- vector[1,i+1] + 1
                }
            }
            
            statistic_exp <- 0 #統計量
            lambda <- 1/mean(data[,1])
            
            for (i in 1:maxsec) {
                theory <- sumfreq * integrate(f_exp, i-1, i, lambda)$value
                realized <- vector[1,i]
                statistic_exp <- statistic_exp + (theory - realized) * (theory - realized) / theory
            }
            theory <- sumfreq * (1 - integrate(f_exp, 0, maxsec, lambda)$value)
            realized <- vector[1,colnum]
            statistic_exp <- statistic_exp + (theory - realized) * (theory - realized) / theory
            
            tails <- matrix(0, nrow=1, ncol=tailnum)
            tails[1,1] <- statistic_exp
            tails[1,2] <- qchisq(df=DF, 0.95)
            if (statistic_exp < qchisq(df=DF, 0.95)) {
                tails[1,3] <- "adopted"
            }
            vector <- cbind(vector, tails)
            table <- rbind(table, vector)
        }
        wfiledir <- paste(maindir, subdir, branch, datayear, "\\table", sep = "", collapse = NULL)
        if (!file.exists(wfiledir)){
            dir.create(wfiledir)
        }
        wfilename <- paste(wfiledir, session, ".csv", sep = "", collapse = NULL)
        write.csv(table[-1,], wfilename, quote = F, row.names = T)
    }
}
