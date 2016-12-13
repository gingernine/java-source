#yearly データのミクロパラメータの統計量のまとめ

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
currentdir <- "\\statistics_of_the_limit_order_book\\yearly"
datayear <- "\\2009"

filepath <- paste(maindir, currentdir, datayear, "_.csv", sep="", collapse=NULL)
data <- read.csv(filepath, sep=",", header=T)

summary <- matrix(0, nrow=12, ncol=7)
colnames(summary) <- c("Mean", "S.D.", "Median", "Kurtosis", "Skewness", "Minimum", "Maximum")
rownames(summary) <- c(colnames(data)[2:9],colnames(data)[14:17])

for (rname in rownames(summary)) {
    vec <- data[,rname]
    Mean <- mean(vec)
    SD <- sd(vec)
    Median <- median(vec)
    Kurtosis <- mean((vec - Mean)^4)/(SD^4)
    Skewness <- mean((vec - Mean)^3)/(SD^3)
    summary[rname,] <- c(Mean, SD, Median, Kurtosis, Skewness, min(vec), max(vec))
}

wfilename <- paste(maindir, currentdir, datayear, "_statistics_summary", ".csv", sep = "", collapse = NULL)
write.csv(summary, wfilename, quote = F, row.names = T)

#tex code
data <- read.csv(wfilename, sep=",", header=T)
numcol <- ncol(data)
numrow <- nrow(data)

code <- ""
for (rowname in colnames(data)[-1]) {
        code <- paste(code, " & ", "{\\", "rm ", rowname, "}", sep="", collapse=NULL)
}
code <- paste(code, " \\ \\hline", sep="", collapse=NULL)
code <- matrix(code, ncol=1, nrow=1)

for (r in seq(numrow)) {
    line <- paste("{\\", "rm ", gsub("\\.", " ", data[r, 1]), "} & ", sep="", collapse=NULL)
    for (c in seq(numcol)[-1]) {
        if (c == numcol) {
            line <- paste(line, "$", data[r, c], "$", " \\ \\hline", sep="", collapse=NULL)
        } else {
            line <- paste(line, "$", data[r, c], "$", " & ", sep="", collapse=NULL)
        }
    }
    code <- rbind(code, matrix(line, nrow=1, ncol=1))
}

wfilename <- paste(maindir, currentdir, datayear, "_texcode", ".csv", sep = "", collapse = NULL)
write.csv(code, wfilename, quote = F, row.names = T)
