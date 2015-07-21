let g:java_classpath=system("./gradlew -q classpath")
let g:syntastic_java_javac_classpath=g:java_classpath
