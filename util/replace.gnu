set lmargin at screen 0.20
set rmargin at screen 0.75
set bmargin at screen 0.30
set tmargin at screen 0.85
set datafile separator " "
set autoscale fix
set key outside right center
set title "requests"
set xlabel "seconds"
set ylabel "requests"
set key autotitle columnhead
set terminal png
set output "replace-request.png"
plot for [n=4:5] "replace.csv" using n:xticlabels(stringcolumn(1)) with lines
