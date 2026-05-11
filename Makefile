TARGET = aarch64-linux-gnu
CFLAGS = -O3 -march=armv8-a+simd -static -lpthread
RMODE  = release

all: server search

server:
	mkdir -p bin
	$(TARGET)-gcc $(CFLAGS) -o bin/ai-server src/server/main.c

search:
	cargo build --$(RMODE) --target $(TARGET)-unknown-linux-gnu
	cp target/$(TARGET)-unknown-linux-gnu/$(RMODE)/search bin/search

clean:
	rm -rf bin/* target/

.PHONY: all server search clean
