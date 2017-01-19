# 板の変動の系列を基に分散を計算する．

permutation01 <- function(n) {
    if (n==0) return(0)
    pat <- matrix(0, nrow=2^n, ncol=n)
    dec2bin <- function(n) {
        #10進数を2進数に変換する．
        ans <- c(0)
        if(n==0) return(ans)
        while(n != 0) {
            a <- n%%2
            ans <- append(ans, a)
            n <- (n - a)/2
        }
        return(ans[-1])
    }
    
    for (i in 0:(2^n-1)) {
        bin <- dec2bin(i)
        for (j in 1:length(bin)) {
            pat[i+1, j] <- bin[j]
        }
    }
    return(pat)
}

