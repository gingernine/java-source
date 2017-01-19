
chebyshev <- function(x, n) {
    return(cos(n * acos(x)))
}


for (n in 1:6) {
    curve(chebyshev(x, n), xlim = c(-1, 1), ylim = c(-2, 1.5), col=n)
    par(new=T)
}

legend("bottomright",
       col=1:6, legend=c("n=1", "n=2", "n=3", "n=4", "n=5", "n=6"),
       lty = c(1,1,1,1,1,1))
