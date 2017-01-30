
maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output\\statistics_of_the_limit_order_book\\move_frequency"
datayear <- "\\2007"
observed <- ""#"_observed"
unit <- 10
morning <- read.csv(paste(maindir, datayear, "\\morning_", unit, "unit_volatility", observed, ".csv", sep="", collapse=NULL ), stringsAsFactors=FALSE, header=F)
afternoon <- read.csv(paste(maindir, datayear, "\\afternoon_", unit, "unit_volatility", observed, ".csv", sep="", collapse=NULL ), stringsAsFactors=FALSE, header=F)
table <- matrix(0, nrow=(nrow(morning)+nrow(afternoon)), ncol=5)
m <- 1
a <- 1
for (r in 1:nrow(table)) {
    if (r==1) {
        for(j in 1:5) {
            table[r,j] <- morning[m, j]
        }
        m <- m+1
    } else {
        if (r%%2==0) {
            for(j in 1:5) {
                table[r,j] <- morning[m, j]
            }
            m <- m+1
        } else if (r%%2==1) {
            for(j in 1:5) {
                table[r,j] <-afternoon[a, j]
            }
            a <- a+1
        }
    }
}

prd <- 0
one <- 0
fiv <- 0
ten <- 0

mean(as.numeric(table[,2]))
sd(as.numeric(table[,2]))
mean(as.numeric(table[,3]))
sd(as.numeric(table[,3]))
mean(as.numeric(table[,4]))
sd(as.numeric(table[,4]))
mean(as.numeric(table[,5]))
sd(as.numeric(table[,5]))

par(lwd=2)
plot(1:length(table[,3]), table[,2], ylim=c(0,0.00013), pch=1, xlab="", ylab="")

plot(1:length(table[,3]), table[,3], ylim=c(0,0.00013), pch=0,
     cex.lab=2,
     xlab="□:1分間隔RV, ○:5分間隔RV, △:10分間隔RV",
     ylab="")
par(new=T)
plot(1:length(table[,3]), table[,4], ylim=c(0,0.00013), pch=1,
     xlab="",
     ylab="")
par(new=T)
plot(1:length(table[,3]), table[,5], ylim=c(0,0.00013), pch=2,
     xlab="",
     ylab="")



#########################発表資料###############################
par(lwd = 2, mar=c(5, 6, 4, 2))
plot(1:length(table[,3]), table[,2], ylim=c(0,0.00013), pch=1,
     col=1,
     cex.axis=3,
     cex.lab=4,
     cex.main=4,
     main="モデル上のボラティリティ(10枚1単位の場合)",
     xlab="時系列",
     ylab="ボラティリティ")
plot(1:length(table[,3]), table[,4], ylim=c(0,0.00013), pch=1,
     col=1,
     cex.axis=3,
     cex.lab=4,
     cex.main=4,
     main="5分間隔リアライズドボラティリティ",
     xlab="時系列",
     ylab="ボラティリティ")

