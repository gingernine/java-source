# 負の二項分布の最尤推定.

negabinomparam <- function(X) {
    #argument X is a vector of observation
    sumX <- sum(X)
    N <- length(X)
    fracsum <- function(n, Xi) {
        i <- 0:(Xi-1)
        return(sum(1/(n+i)))
    }
    fracsum2 <- function(n, Xi) {
        i <- 0:(Xi-1)
        return(sum(1/(n+i)^2))
    }
    n <- 5
    tmp <- n
    diff <- 1
    while(diff >= 1e-10) {
        fs <- 0
        fs2 <- 0
        for(i in 1:N) {
            fs <- fs + fracsum(n, X[i])
            fs2 <- fs2 + fracsum2(n, X[i])
        }
        n <- n - (N*(log(n*N) - log(sumX+n*N)) + fs) / (N/n - N*N/(sumX + n*N) - fs2)
        print(n)
        diff <- abs(tmp - n)
        tmp <- n
    }
    return(list(size = n, prob = n*N / (sumX + n*N)))
}

n <- 10
p <- 0.4
X <- 1:30000
for (i in X){
    X[i] <- rnbinom(1, n, p)
}

negabinomparam(X)
