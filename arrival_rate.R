#注文時間間隔及のtexcodeテーブルを作成する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\time_interval\\"
datayear <- "\\2007"
branchs <- c( "time_interval_limit_buy", "time_interval_limit_sell", "time_interval_market_buy", "time_interval_market_sell" )
sessions <- c( "\\morning", "\\afternoon" )

table <- matrix(0, nrow=1, ncol=1)
i <- 1
for (branch in branchs) {
    Mean <- c(0)
    for (session in sessions) {
        rfilepath <- paste(maindir, currentdir, branch, datayear, "\\statistics_summary", session, ".csv", sep="", collapse=NULL)
        data <- read.csv(rfilepath, sep=",", header=T)
        Mean <- c(Mean, data[,"Mean"])
    }
    Mean <- Mean[-1]
    if (length(table[,1]) == 1) {
        table <- matrix(0, nrow=length(Mean), ncol=6)
        colnames(table) <- c("lambda_B","lambda_A","mu_A","mu_B", "rho_B", "rho_A")
    }
    table[,i] <- Mean
    i <- i+1
}

for (r in seq(nrow(table))) {
    for (c in 1:4) {
        table[r,c] <- 1/table[r,c]
    }
    table[r,5] <- table[r,1]/table[r,4]
    table[r,6] <- table[r,2]/table[r,3]
}

summary <- matrix(0, ncol=6, nrow=2)
rownames(summary) <- c("Mean", "S.D.")
colnames(summary) <- c("lambda_B","lambda_A","mu_A","mu_B", "rho_B", "rho_A")
for (rname in colnames(summary)) {
    summary["Mean", rname] <- mean(table[,rname])
    summary["S.D.", rname] <- sd(table[,rname])
}


#tex code
code <- ""
for (cname in colnames(summary)) {
    code <- paste(code, " & ", "\\", cname, sep="", collapse=NULL)
}
code <- paste(code, " \\ \\hline", sep="", collapse=NULL)
code <- matrix(code, ncol=1, nrow=1)

for (rname in rownames(summary)) {
    line <- paste("{\\", "rm ", rname, "} & ", sep="", collapse=NULL)
    for (c in seq(ncol(summary))) {
        if (c == ncol(summary)) {
            line <- paste(line, "$", summary[rname, c], "$", " \\ \\hline", sep="", collapse=NULL)
        } else {
            line <- paste(line, "$", summary[rname, c], "$", " & ", sep="", collapse=NULL)
        }
    }
    code <- rbind(code, matrix(line, nrow=1, ncol=1))
}

wfiledir <- paste(maindir, currentdir, "\\summary", sep="", collapse=NULL)
if (!file.exists(wfiledir)) {
    dir.create(wfiledir)
}

wfilepath <- paste(maindir, currentdir, "\\summary", datayear, "_texcode.csv", sep="", collapse=NULL)
write.csv(code, wfilepath, quote = F, row.names = T)

