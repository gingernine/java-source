#モデルによる期待値と実際の値で，変動の頻度を比較する．

f_A <- function(t, r_A, l_A, m_A) {
    exp(-(l_A+m_A)*t) * r_A/t * (m_A/l_A)^(r_A/2) * besselI(2*t*sqrt(l_A*m_A), r_A)
}

f_B <- function(t, r_B, l_B, m_B) {
    exp(-(l_B+m_B)*t) * r_B/t * (m_B/l_B)^(r_B/2) * besselI(2*t*sqrt(l_B*m_B), r_B)
}

f_A_Exp <- function(t, r_A, l_A, m_A) {
    t * f_A(t, r_A, l_A, m_A)
}

f_B_Exp <- function(t, r_B, l_B, m_B) {
    t * f_B(t, r_B, l_B, m_B)
}

f_U <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_A(t, r_A, l_A, m_A) - f_A(t, r_A, l_A, m_A) * integrate(f_B, 0, t, r_B, l_B, m_B)$value
}

f_D <- function(t, r_A, l_A, m_A, r_B, l_B, m_B) {
    f_B(t, r_B, l_B, m_B) - f_B(t, r_B, l_B, m_B) * integrate(f_A, 0, t, r_A, l_A, m_A)$value
}

