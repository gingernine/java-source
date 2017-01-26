#実際に観測された推移確率の計算する．

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output\\statistics_of_the_limit_order_book"
subdir <- "\\transition_probability"
datayear <- "\\2007"
subdir2 <- "\\observed"

sessions <- c( "\\morning", "\\afternoon" )

table <- matrix(0, ncol=4, nrow=1)
cortab <- matrix(0, ncol=1, nrow=1)
for (session in sessions) {
    rdirpath <- paste(maindir, subdir, datayear, subdir2, session, sep="", collapse=NULL)
    for (name in list.files(rdirpath)) {
        rfilepath <- paste(rdirpath, "\\", name, sep="", collapse=NULL)
        data <- read.csv(rfilepath, header=F)
        vector <- matrix(0, ncol=4, nrow=1)
        corr <- matrix(0, ncol=2, nrow=nrow(data)-1)
        tmp = data[1,1]
        p <- 1
        for (i in 2:length(data[,1])) {
            d <- data[i,1]
            if (d=="up" && tmp=="up") {
                vector[1,1] <- vector[1,1]+1
                corr[p,] <- c(1,1)
            } else if (d=="down" && tmp=="up") {
                vector[1,2] <- vector[1,2]+1
                corr[p,] <- c(1,0)
            } else if (d=="up" && tmp=="down") {
                vector[1,3] <- vector[1,3]+1
                corr[p,] <- c(0,1)
            } else if (d=="down" && tmp=="down") {
                vector[1,4] <- vector[1,4]+1
                corr[p,] <- c(0,0)
            }
            p <- p+1
            tmp <- d
        }
        c <- matrix(0,1,1)
        c[1,1] <- cor(corr[,1], corr[,2], method="spearman")
        cortab <- rbind(cortab, c)
        rownames(vector) <- c(paste(substr(name, 1,4), substr(name, 5,6), substr(name, 7,8), substr(session,2,10), sep="/", collapse=NULL))
        colnames(vector) <- c( "p_UU", "p_UD", "p_DU", "p_DD")
        table <- rbind(table, vector)
    }
}

cortab <- cortab[-1,]
table <- table[-1,]
probmat <- matrix(0, ncol=ncol(table), nrow=nrow(table))
rownames(probmat) <- rownames(table)
colnames(probmat) <- colnames(table)
for (i in 1:nrow(probmat)) {
    probmat[i,1] <- table[i,1] / (table[i,1]+table[i,2])
    probmat[i,2] <- table[i,2] / (table[i,1]+table[i,2])
    probmat[i,3] <- table[i,3] / (table[i,3]+table[i,4])
    probmat[i,4] <- table[i,4] / (table[i,3]+table[i,4])
}

mean(probmat[,1])
sqrt(sd(probmat[,1]))
mean(probmat[,2])
sqrt(sd(probmat[,2]))
mean(probmat[,3])
sqrt(sd(probmat[,3]))
mean(probmat[,4])
sqrt(sd(probmat[,4]))

wfilepath <- "C:\\Users\\kklab\\Desktop\\yurispace\\integration_cpp\\source\\2007\\probability_observed.csv"
write.csv(probmat, wfilepath, row.names=T, quote=F)