default: target/uberjar/signal-hook.jar

test:
	lein test

build: target/uberjar/signal-hook.jar

target/uberjar/signal-hook.jar:
	lein uberjar

docker: target/uberjar/signal-hook.jar
	docker build -t signal-hook .

clean:
	lein clean

