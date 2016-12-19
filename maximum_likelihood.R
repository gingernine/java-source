
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

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
subdir <- "\\statistics_of_the_limit_order_book\\initial_depth"
datayear <- "\\2007\\"
tails <- c( "after_up_", "after_up_" )
bidask <- c( "bid.depth", "bid.depth" )

for (tail in tails) {
    filepath <- paste(maindir, subdir, datayear, tail, ".csv", sep="", collapse=NULL)
    data <- read.csv(filepath, header=T)
    
    for (ba in bidask) {
        X <- numeric(max(data[, ba]))
        for (i in data[, ba]) {
            X[i] <- X[i] + 1
        }
        barplot(X)
        s <- sum(X)
        for (i in seq(length(X))) {
            X[i] <- X[i] / s
        }
        alpha <- gammaparam(data[, ba])$alpha
        beta <- gammaparam(data[, ba])$beta
        
        barplot(X, xlim=c(0, 50), ylim=c(0, 0.1))
        par(new=T)
        curve(g(x, alpha, beta), xlim=c(0, 50), ylim=c(0, 0.1), col=2)
    }
}

