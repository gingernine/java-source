# 負の相関とボラティリティの関係を調べる．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output\\statistics_of_the_limit_order_book\\move_frequency"
datayear <- "\\2007"
unit <- 1
morning <- read.csv(paste(maindir, datayear, "\\morning_", unit, "unit_volatility", ".csv", sep="", collapse=NULL ), stringsAsFactors=FALSE, header=F)
afternoon <- read.csv(paste(maindir, datayear, "\\afternoon_", unit, "unit_volatility", ".csv", sep="", collapse=NULL ), stringsAsFactors=FALSE, header=F)
table <- rbind(morning, afternoon)

vect <- matrix(0, nrow=(nrow(table)), ncol=1)
for (i in 1:nrow(table)) {
    vect[i, 1] <- table[i, 1]
}

probdir <- "C:\\Users\\kklab\\Desktop\\yurispace\\integration_cpp\\source"
probmat <- read.csv(paste(probdir,datayear, "\\probability3_", unit, "pieces.csv", sep="", collapse=NULL),
                    stringsAsFactors=FALSE, row.names = 1, header=T)

bind <- matrix(0, ncol=1, nrow=nrow(probmat)-5)
j <- 1
for (i in 1:nrow(probmat)) {
    if (i >= 255 && i <= 259) {
        next
    }
    pvec <- probmat[i, ]
    p_UU <- as.numeric(pvec[1])
    p_UD <- as.numeric(pvec[2])
    p_DU <- as.numeric(pvec[3])
    p_DD <- as.numeric(pvec[4])
    p_U <- p_DU / (p_DU + p_UD)
    p_D <- p_UD / (p_DU + p_UD)
    bind[j, 1] <- (p_U*(p_UU-p_UD) - p_D*(p_DU-p_DD) - (p_U-p_D)*(p_U-p_D)) / (p_U + p_D - (p_U-p_D)*(p_U-p_D))
    j <- j+1
}

mean(bind[,1], na.rm=T)
sd(bind[,1], na.rm=T)

#####################################発表資料#########################################
maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output\\statistics_of_the_limit_order_book\\move_frequency"
datayear <- "\\2007"
name <- "\\volatility_curve.csv"

data <- read.csv(paste(maindir, datayear, name, sep="", collapse=NULL), header=T)
par(new=F, mar=c(5, 6, 4, 2))
for (i in 1:5) {
    column <- paste("X", 100*i, sep="", collapse=NULL)
    plot(data[,"correlation"], data[,column], xlim=c(-1, 0), ylim=c(0, 0.0001),
         cex.axis=3, cex.lab=3, pch=20, col=i, ylab="ボラティリティ", xlab="系列相関", main="")
    text(x=-0.2, y=data[600,column]-1e-05, label=paste(100*i, "回変動", sep="", collapse=NULL), cex=4, col=i)
    par(new=T)
}

abline(v=-0.9, col=2)
abline(v=-0.35)
