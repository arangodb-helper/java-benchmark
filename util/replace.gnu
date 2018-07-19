set lmargin at screen 0.20
set rmargin at screen 0.75
set bmargin at screen 0.30
set tmargin at screen 0.85
set datafile separator " "
set autoscale fix
set key outside right center
set title "hunde"
set xlabel "seconds"
set ylabel "requests"
set terminal png
set output "replace-request.png"
plot "replace.csv" using 4:xticlabels(stringcolumn(1)) title "REQUESTS" with line
