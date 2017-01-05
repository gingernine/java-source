
library(Rmpfr)

factorial <- function(l) {
    if (l==0) {
		return(1)
    } else if (l > 0) {
		return(prod(1:l))
	}
}

legendre <- function(x, n) {
	sum <- 0
	for (k in 0:floor(n/2)) {
		numer <- factorial(2*n - 2*k) * (-1)^k * x^(n - 2*k)
		denom <- factorial(n - 2*k) * factorial(k) * factorial(n - k)
		sum <- sum + numer/denom
	}
	return(sum / 2^n)
}

for (n in 20:32) {
	curve(legendre(x, n), xlim = c(-1, 1), ylim = c(-1, 1), col=n)
	par(new=T)
}

legend("bottomright",
       col=1:6, legend=c("n=1", "n=2", "n=3", "n=4", "n=5", "n=6"),
       lty = c(1,1,1,1,1,1))

legendre_deriv <- function(x, n) {
    # subject to n >= 1 #
    # equation (23) #
    return( n * (x * legendre(x, n) - legendre(x, n-1)) / (x^2 - 1) )
}

newton_method <- function(n) {
    y <- numeric(n)
    for(i in 1:n) {
        x <- cos((i-0.25)*pi/(n+0.5))
        tmp <- 1
        for(j in 1:1000) {
            tmp <- x
            x <- x - legendre(x, n) / legendre_deriv(x, n)
        }
        y[i] <- x
    }
    return(y)
}

table <- matrix(0, ncol=100, nrow=100)
for (i in 1:1000) {
    table[1:i,i] <- newton_method(i)
    print(i)
}

wfilepath <- "C:\\Users\\kklab\\Desktop\\yurispace\\legendre_roots.csv"
write.csv(table, wfilepath, quote = F, row.names = F)
