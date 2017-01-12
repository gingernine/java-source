#yearly データのミクロパラメータの統計量のまとめ

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
subdir <- "\\statistics_of_the_limit_order_book\\yearly"
datayear <- "\\2007"

filepath <- paste(maindir, currentdir, datayear, "_.csv", sep="", collapse=NULL)
data <- read.csv(filepath, sep=",", header=T)

summary <- matrix(0, nrow=16, ncol=7)
colnames(summary) <- c("Mean", "S.D.", "Median", "Kurtosis", "Skewness", "Minimum", "Maximum")
rownames(summary) <- c(colnames(data)[2:17])

for (rname in rownames(summary)) {
    vec <- data[,rname]
    Mean <- mean(vec)
    SD <- sd(vec)
    Median <- median(vec)
    Kurtosis <- mean((vec - Mean)^4)/(SD^4)
    Skewness <- mean((vec - Mean)^3)/(SD^3)
    summary[rname,] <- c(Mean, SD, Median, Kurtosis, Skewness, min(vec), max(vec))
}

wfilename <- paste(maindir, subdir, datayear, "_statistics_summary", ".csv", sep = "", collapse = NULL)
write.csv(summary, wfilename, quote = F, row.names = T)

#tex code

code <- ""
for (colname in colnames(summary)) {
        code <- paste(code, " & ", "{\\", "rm ", colname, "}", sep="", collapse=NULL)
}
code <- paste(code, " \\\\ \\hline", sep="", collapse=NULL)
code <- matrix(code, ncol=1, nrow=1)

for (r in seq(nrow(summary))) {
    line <- paste("{\\", "rm ", gsub("\\.", " ", rownames(summary)[r]), "} & ", sep="", collapse=NULL)
    for (c in seq(ncol(summary))) {
        if (c == ncol(summary)) {
            line <- paste(line, "$", summary[r, c], "$", " \\\\ \\hline", sep="", collapse=NULL)
        } else {
            line <- paste(line, "$", summary[r, c], "$", " & ", sep="", collapse=NULL)
        }
    }
    code <- rbind(code, matrix(line, nrow=1, ncol=1))
}

wfilename <- paste(maindir, subdir, datayear, "_texcode", ".csv", sep = "", collapse = NULL)
write.csv(code, wfilename, quote = F, row.names = T)
