
#?K???}???z?Ìƒp?????[?^?ÌÅ–Ş?????Ê???v?Z?????D
gammaparam <- function(X) {
    #?f?B?K???}?Ö???Ì???Ö????¿½?¿½?¿½?¿½??`???ÅŒv?Z?????D
    ddigamma <- function(x) {
        sum <- 0
        n <- 0
        while( 1 / (x + n)^2 >= 1e-08 ) {
            sum <- sum + 1 / (x + n)^2
            n <- n + 1
        }
        return(sum)
    }
    mlogx <- mean(log(X))
    logmx <- log(mean(X))

    #?j???[?g???@?É?????Ä‹A?v?Z.
    iter <- function(a) {
        mlogx - logmx + log(a) - digamma(a)
    }
    a <- exp(-1)
    while( abs(tmp - a) >= 1e-08 ) {
        tmp <- a
        a <- a - iter(a) / (1/a - ddigamma(a))
    }
    return(list(alpha = a, beta = mean(X) / a))
}

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
    n <- 1e-1
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

#Gamma density function
gammadense <- function(x, alpha, beta) {
    return(1/(gamma(alpha) * beta^alpha) * x^(alpha-1) * exp(-x/beta))
}

pois <- function(x, lambda) {
    return(lambda^x/gamma(x) * exp(-lambda))
}

geom <- function(x, p) {
    return(p * (1-p)^x)
}

nbinom <- function(x, n, p) {
    return(choose(n+x-1, x) * p^n * (1-p)^x)
}

maindir <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output"
subdir <- "\\statistics_of_the_limit_order_book\\initial_depth"
datayear <- "\\2007\\"
tails <- c( "after_up_", "after_down_" )
dists <- c( "gamma_bid.depth", "gamma_ask.depth", "poiss_bid.depth", "poiss_ask.depth", 
             "nbinm_bid.depth", "nbinm_ask.depth", "geome_bid.depth", "geome_ask.depth" )

for (tail in tails) {
    filepath <- paste(maindir, subdir, datayear, tail, ".csv", sep="", collapse=NULL)
    data <- read.csv(filepath, header=T)
    table <- matrix(0, ncol=2, nrow=8)
    colnames(table) <- c( "alpha(size)", "beta(prob)" )
    rownames(table) <- dists

    for (d in dists) {
        dist <- substr(d, 1, 5)
        ba <- substr(d, 7, 100)
        X <- numeric(max(data[, ba]))
        for (i in data[, ba]) {
            X[i] <- X[i] + 1
        }

        s <- sum(X)
        for (i in seq(length(X))) {
            X[i] <- X[i] / s
        }
        
        if (dist == "gamma") {
            alpha <- gammaparam(data[, ba])$alpha
            beta <- gammaparam(data[, ba])$beta
        } else if (dist == "poiss") {
            alpha <- mean(data[, ba]-1)
            beta <- 0
        } else if (dist == "nbinm") {
            ret <- negabinomparam(data[,ba]-1)
            alpha <- ret$size
            beta <- ret$prob
        } else if (dist == "geome") {
            alpha <- 1 / (1 + mean(data[, ba]-1))
            beta <- 0
        }
        table[d, ] <- c(alpha, beta)

        pngdir <- paste(maindir, subdir, datayear, "maximum_likelihood", sep = "", collapse = NULL)
        if (!file.exists(pngdir)) {
            dir.create(pngdir)
        }
        pngname <- paste(pngdir, "\\", tail, d, ".png", sep = "", collapse = NULL)
        png(pngname)
        xmin <- 1e-01
        xmax <- length(X)
        ymax <- max(X)
        plot(X, xlim=c(xmin, xmax), ylim=c(0, ymax), type="h",
             main=paste(tail, ba, "\n alpha=", alpha, ", beta=", beta, sep = "", collapse = NULL), ylab="probability", xlab="initial depth")
        par(new=T)
        if (dist == "gamma") {
            curve(gammadense(x, alpha, beta), xlim=c(xmin, xmax), ylim=c(0, ymax), col=2,
                  main="", xlab="", ylab="")
        } else if (dist == "poiss") {
            plot(dpois(1:xmax -1, alpha), xlim=c(xmin, xmax), ylim=c(0, ymax), col=2, type="l",
                 main="", xlab="", ylab="")
        } else if (dist == "nbinm") {
            plot(dnbinom(1:xmax -1, alpha, beta), xlim=c(xmin, xmax), ylim=c(0, ymax), col=2, type="l",
                 main="", xlab="", ylab="")
        } else if (dist == "geome") {
            plot(dgeom(1:xmax -1, alpha), xlim=c(xmin, xmax), ylim=c(0, ymax), col=2, type="l",
                 main="", xlab="", ylab="")
        }
        dev.off()
    }
    wfilepath <- paste(pngdir, "\\parameters_", tail, ".csv", sep = "", collapse = NULL)
    write.csv(table, wfilepath, quote = F, row.names = T, col.names = T)
}

