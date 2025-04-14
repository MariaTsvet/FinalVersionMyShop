#!/bin/sh
export PATH_TO_FX=~/Library/Java/JavaVirtualMachines/javafx-sdk-23.0.1/lib
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -cp target/fxapp-1.0-SNAPSHOT.jar nntc.svg52.fxapp.HelloApplication