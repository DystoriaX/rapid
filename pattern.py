import os

projects = list(filter(lambda name: "cassandra" in name, os.listdir("Benchmarks/")))
os.chdir("Benchmarks")
print(projects)
for project in projects:
    os.chdir(project)
    tests = list(filter(lambda name: "TEST" in name, os.listdir(".")))
    for test in tests:
        os.chdir(test)
        if not os.path.exists("pattern"):
            os.makedirs("pattern")
        os.system("java -classpath \"$CLASSPATH:/Users/askarzhendongang/Code/rapid/bin:/Users/askarzhendongang/Code/rapid/lib/*\" PatternGeneration -f rr -p " + test + ".rr\n")
        os.chdir("..")
    os.chdir("..")