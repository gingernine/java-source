# calculate the expectations

rootpath <- "C:\\Users\\kklab\\Desktop\\yurispace\\integration_cpp\\source"
contpath <- "C:\\Users\\kklab\\Desktop\\yurispace\\board_fluctuation\\src\\nikkei_needs_output\\statistics_of_the_limit_order_book"
subdir <- "\\arrival_time_series"
contpath2 <- paste(contpath, subdir, datayear, "\\limit_buy", sep="", collapse=NULL)
datayear <- "\\2007"
movmorning <- read.csv(paste(contpath, "\\move_frequency", datayear, "\\morning1min_int.csv", sep="", collapse=NULL), header=F, row.names = 1)
movafternoon <- read.csv(paste(contpath, "\\move_frequency", datayear, "\\afternoon1min_int.csv", sep="", collapse=NULL), header=F, row.names = 1)

units <- c( "1", "10", "30" )

for (unit in units) {
    exppath <- paste(rootpath, datayear, "\\Exp2_", unit, "pieces.csv", sep="", collapse=NULL)
    expdata <- read.csv(exppath, stringsAsFactors=FALSE, header=T, row.names = 1)
    probpath <- paste(rootpath, datayear, "\\probability2_", unit, "pieces.csv", sep="", collapse=NULL)
    probdata <- read.csv(probpath, stringsAsFactors=FALSE, header=T, row.names = 1)
    
    table <- matrix(0, ncol=5, nrow=length(probdata[,1]))
    colnames(table) <- c("date", "E", "continuoustime", "movfreq_model", "movefreq_real")
    p=1
    for (i in 1:length(expdata[,1])) {
        r <- rownames(expdata)[i]
        session <- substr(r, 12, 100)
        date <- paste(substr(r, 1, 4), substr(r, 6, 7), substr(r, 9, 10), sep="", collapse=NULL)
        if (session == "morning") {
            realmove <- sum(movmorning[date,], na.rm=T)
        } else if(session == "afternoon") {
            realmove <- sum(movafternoon[date,], na.rm=T)
        }
        if (substr(r, 1, 4) != "2007") {
            next
        }
        contdata <- read.csv(paste(contpath2, "\\", session, "\\", date, "_.csv", sep="", collapse=NULL), header=F)
        continuoustime <- contdata[1, 4]
        
        Eu <- as.numeric(expdata[r,1])
        Ed <- as.numeric(expdata[r,2])
        
        if(is.na(Eu) || is.infinite(Eu) || is.na(Ed) || is.infinite(Ed)) {
            table[p, ] <- c(r, Inf, continuoustime, 0, realmove)
            p <- p+1
            next
        }
        
        p_UU <- as.numeric(probdata[r, "p_UU"])
        p_UD <- as.numeric(probdata[r, "p_UD"])
        p_DU <- as.numeric(probdata[r, "p_DU"])
        p_DD <- as.numeric(probdata[r, "p_DD"])
        
        p_U <- p_DU / (p_DU + p_UD)
        p_D <- p_UD / (p_DU + p_UD)
        E <- p_U*Eu + p_D*Ed
        table[p, ] <- c(r, E, continuoustime, floor(continuoustime/E), realmove)
        p <- p+1
    }
    wfilepath <- paste(rootpath, datayear, "\\Expectations_", unit, "pieces.csv", sep="", collapse=NULL)
    write.csv(table, wfilepath, quote = F, row.names = F)
}