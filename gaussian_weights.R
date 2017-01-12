# calculate the weights for gaussian integration

library(Rmpfr)

combination <- function(n, r) {
    if (r==0) {
        return(1)
    } else {
        i <- 0:(r-1)
        ret <- prod((n-i) / (i+1))
        return(ret)
    }
}

factorial <- function(n) {
    if (n==0) {
        return(1)
    } else if (n > 0) {
        return(prod(1:n))
    }
}

laguerre <- function(x, n) {
    s <- 0
    for (r in 0:n) {
        s <- s + (-x)^r * combination(n, r) * factorial(n) / factorial(r)
    }
    return(s)
}

lagweights <- function(x, n) {
    y = factorial(n) / laguerre(x, n+1)
    return(y*y*x)
}

data <- read.csv("C:\\Users\\kklab\\Desktop\\yurispace\\integration_cpp\\gauss_roots\\lagroots.csv", header=T)
vec <- matrix(0, nrow=nrow(data), ncol=2)
for (i in 1:nrow(data)) {
    w <-lagweights(data[i,2], data[i,1])
    vec[i,] <- c(data[i,1], w)
}

colnames(vec) <- c("n", "weight")
write.csv(vec, "C:\\Users\\kklab\\Desktop\\yurispace\\integration_cpp\\gauss_roots\\lagweights.csv", row.names = F)
