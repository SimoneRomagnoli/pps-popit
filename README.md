# pps-popit
`pps-popit` is a reproduction of the single-player game
[Bloons TD Battle](https://www.microsoft.com/en-us/p/bloons-td-battles/9nblggh6cwkl?activetab=pivot:overviewtab).
In the game, players try to prevent balloons from reaching the end of a path by placing towers that can pop the balloons.
The balloons' waves come in different rounds with increasing level of difficulty.

This project is developed for academic purposes for the course`Paradigmi di Programmazione e Sviluppo`
of the master's degree `Ingegneria e Scienze Informatiche` of `University of Bologna` under the academic year 2020/2021.

## Requirements
The following dependencies are required to play `pps-popit`:
- SBT v1.5.5 - Build tool required to execute the source code or tests
- Scala v2.13.6
- JVM >= v1.11 - Java Virtual Machine on which is executed Scala

## Usage
You can find the latest `jar` of the application inside the [`GitHub Release` section](https://github.com/SimoneRomagnoli/pps-popit/releases).
To execute the application, simply run:
```
$ java -jar `path-to-downloaded-jar`
```

Alternatively, you can clone the repository and execute the following commands to generate the `jar` executable file:
```
$ sbt compile
$ sbt assembly
```

## Test
You can execute tests with the command:
```
$ sbt test
```

## Report
You can download the updated report on the developed project [here](https://www.overleaf.com/download/project/614475489b79f336b3888962/build/17c22518167-58c17a362d0e1599/output/output.pdf?compileGroup=priority&clsiserverid=clsi-pre-emp-e2-e-vkwd&popupDownload=true). 

## Authors
- Simone Romagnoli ([SimoneRomagnoli](https://github.com/SimoneRomagnoli))
- Alessandro Marcantoni ([alessandro-marcantoni](https://github.com/alessandro-marcantoni))
- Tommaso Mandoloni ([TommasoMandoloni](https://github.com/TommasoMandoloni))
- Matteo Ragazzini ([MatteoRagazzini](https://github.com/MatteoRagazzini))

Some icons from the original game are from [Bloons Wiki](https://bloons.fandom.com/wiki/Bloons_TD_Battles).