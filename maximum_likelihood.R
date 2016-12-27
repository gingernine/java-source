
#ガンマ分布のパラメータの最尤推定量を計算する．
gammaparam <- function(X) {
    #ディガンマ関数の導関数を級数形式で計算する．
    ddigamma <- function(x) {
        sum <- 0
        n <- 0
        while(T) {
            sum <- sum + 1 / (x + n)^2
            n <- n + 1
            if (1 / (x + n)^2 < 1e-08) {
                break #ある程度の誤差で打ち止め．
            }
        }
        return(sum)
    }
    mlogx <- mean(log(X))
    logmx <- log(mean(X))
    
    #ニュートン法による再帰計算.
    iter <- function(a) {
        mlogx - logmx + log(a) - digamma(a)
    }
    a <- exp(-1)
    while(T) {
        tmp <- a
        a <- a - iter(a) / (1/a - ddigamma(a))
        if (abs(tmp - a) < 1e-08){
            break #ある程度の誤差で打ち止め．
        }
    }
    return(list(alpha = a, beta = mean(X) / a)) 
}

#Gamma density function 
gammadense <- function(x, alpha, beta) {
    return(1/(gamma(alpha) * beta^alpha) * x^(alpha-1) * exp(-x/beta))
}

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
subdir <- "\\statistics_of_the_limit_order_book\\initial_depth"
datayear <- "\\2007\\"
tails <- c( "after_up_", "after_down_" )
bidask <- c( "bid.depth", "ask.depth" )

for (tail in tails) {
    filepath <- paste(maindir, subdir, datayear, tail, ".csv", sep="", collapse=NULL)
    data <- read.csv(filepath, header=T)
    table <- matrix(0, ncol=2, nrow=2)
    colnames(table) <- c( "alpha", "beta" )
    rownames(table) <- c( "bid.depth", "ask.depth" )
    
    for (ba in bidask) {
        X <- numeric(max(data[, ba]))
        for (i in data[, ba]) {
            X[i] <- X[i] + 1
        }
        
        s <- sum(X)
        for (i in seq(length(X))) {
            X[i] <- X[i] / s
        }
        
        alpha <- gammaparam(data[, ba])$alpha
        beta <- gammaparam(data[, ba])$beta
        table[ba, ] <- c(alpha, beta)
        
        pngdir <- paste(maindir, subdir, datayear, "maximum_likelihood", sep = "", collapse = NULL)
        if (!file.exists(pngdir)) {
            dir.create(pngdir)
        }
        pngname <- paste(pngdir, "\\", tail, ba, ".png", sep = "", collapse = NULL)
        png(pngname)
        xmin <- 1e-01
        xmax <- length(X)
        ymax <- max(X)
        plot(X, xlim=c(xmin, xmax), ylim=c(0, ymax), type="h", 
             main=paste(tail, ba, "\n alpha=", alpha, ", beta=", beta, sep = "", collapse = NULL), ylab="probability", xlab="initial depth")
        par(new=T)
        curve(gammadense(x, alpha, beta), xlim=c(xmin, xmax), ylim=c(0, ymax), col=2,
              main="", xlab="", ylab="")
        dev.off()
    }
    wfilepath <- paste(pngdir, "\\parameters_", tail, ".csv", sep = "", collapse = NULL)
    write.csv(table, wfilepath, quote = F, row.names = T, col.names = T)
}

