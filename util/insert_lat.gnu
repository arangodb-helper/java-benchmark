set lmargin at screen 0.20
set rmargin at screen 0.75
set bmargin at screen 0.30
set tmargin at screen 0.85
set datafile separator ","
set autoscale fix
set key outside right center
set title "latencies"
set xlabel "runtime (s)"
set ylabel "ms"
set key autotitle columnhead
set terminal png
set output "insert-latency.png"
plot for [n=6:11] "insert.csv" using n:xticlabels(stringcolumn(1)) with lines
