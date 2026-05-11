#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <pthread.h>

#define PORT 8080
#define BUFSIZE 65536

typedef struct { int fd; char buf[BUFSIZE]; } Conn;

char* infer(const char* prompt) {
    char cmd[4096];
    snprintf(cmd, sizeof(cmd),
        "./llama-cli -m model.gguf -p \"%s\" "
        "--n-predict 512 --threads 4 "
        "--ctx-size 2048 --log-disable 2>/dev/null",
        prompt);
    FILE* p = popen(cmd, "r");
    if (!p) return strdup("{\"error\":\"inference failed\"}");
    char* out = calloc(1, BUFSIZE);
    char line[1024];
    size_t total = 0;
    while (fgets(line, sizeof(line), p) && total < BUFSIZE - 1) {
        size_t n = strlen(line);
        memcpy(out + total, line, n);
        total += n;
    }
    pclose(p);
    return out;
}

void* handle(void* arg) {
    Conn* c = arg;
    char* p = strstr(c->buf, "\"prompt\":\"");
    if (!p) { close(c->fd); free(c); return NULL; }
    p += 10;
    char prompt[2048] = {0};
    for (int i = 0; *p && *p != '"' && i < 2047; i++)
        prompt[i] = *p++;
    char* result = infer(prompt);
    char resp[BUFSIZE + 256];
    snprintf(resp, sizeof(resp),
        "HTTP/1.1 200 OK\r\n"
        "Content-Type: application/json\r\n"
        "Access-Control-Allow-Origin: *\r\n\r\n"
        "{\"response\":\"%s\"}", result);
    send(c->fd, resp, strlen(resp), 0);
    free(result);
    close(c->fd);
    free(c);
    return NULL;
}

int main(void) {
    int sfd = socket(AF_INET, SOCK_STREAM, 0);
    int opt = 1;
    setsockopt(sfd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
    struct sockaddr_in addr = {
        .sin_family = AF_INET,
        .sin_port   = htons(PORT),
        .sin_addr.s_addr = 0
    };
    bind(sfd, (struct sockaddr*)&addr, sizeof(addr));
    listen(sfd, 16);
    printf("[server] listening on :%d\n", PORT);
    for (;;) {
        Conn* c = malloc(sizeof(Conn));
        c->fd = accept(sfd, NULL, NULL);
        recv(c->fd, c->buf, BUFSIZE - 1, 0);
        pthread_t t;
        pthread_create(&t, NULL, handle, c);
        pthread_detach(t);
    }
}
