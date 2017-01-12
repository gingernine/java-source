
combination <- function(n, r) {
    if (r==0) {
        return(1)
    } else {
        i <- 0:(r-1)
        ret <- prod((n-i) / (i+1))
        return(ret)
    }
}

factorial <- function(l) {
    if (l==0) {
        return(1)
    } else if (l > 0) {
        return(prod(1:l))
    }
}

laguerre <- function(x, n) {
    s <- 0
    for (r in 0:n) {
        s <- s + (-1)^r * combination(n, r) * factorial(n) / factorial(r) * x^r
    }
    return(s * exp(-x/2) / factorial(n))
}

par(new=F)
for (n in 1:6) {
    curve(laguerre(x, n), xlim = c(-30, 30), ylim = c(-100, 100), col=n)
    par(new=T)
}

legend("topright",
       col=1:6, legend=c("n=1", "n=2", "n=3", "n=4", "n=5", "n=6"),
       lty = c(1,1,1,1,1,1))
