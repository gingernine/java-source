#注文時間間隔及のtexcodeテーブルを作成する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
subdir <- "\\statistics_of_the_limit_order_book\\arrival_time_series"
datayear <- "\\2007"

filename <- "arrival_rate_per_30pieces"
rfilepath <- paste(maindir, subdir, datayear, "\\", filename, ".csv", sep="", collapse=NULL)
table <- read.csv(rfilepath, header=T)

summary <- matrix(0, ncol=4, nrow=2)
rownames(summary) <- c("Mean", "S.D.")
colnames(summary) <- c("lambda_B","lambda_A","mu_A","mu_B" )
for (rname in colnames(summary)) {
    summary["Mean", rname] <- mean(table[,rname])
    summary["S.D.", rname] <- sd(table[,rname])
}


#tex code
code <- paste("{\\", "rm Arrival Rate Per Second}", sep="", collapse=NULL)
for (cname in colnames(summary)) {
    code <- paste(code, " & ", "\\", cname, sep="", collapse=NULL)
}
code <- paste(code, " \\\\ \\hline", sep="", collapse=NULL)
code <- matrix(code, ncol=1, nrow=1)

for (rname in rownames(summary)) {
    line <- paste("{\\", "rm ", rname, "} & ", sep="", collapse=NULL)
    for (c in seq(ncol(summary))) {
        if (c == ncol(summary)) {
            line <- paste(line, "$", summary[rname, c], "$", " \\\\ \\hline", sep="", collapse=NULL)
        } else {
            line <- paste(line, "$", summary[rname, c], "$", " & ", sep="", collapse=NULL)
        }
    }
    code <- rbind(code, matrix(line, nrow=1, ncol=1))
}

wfiledir <- paste(maindir, subdir, datayear, "\\summary_texcode", sep="", collapse=NULL)
if (!file.exists(wfiledir)) {
    dir.create(wfiledir, recursive=T)
}

wfilepath <- paste(wfiledir, "\\", filename, "_texcode.csv", sep="", collapse=NULL)
write.csv(code, wfilepath, quote = F, row.names = T)
