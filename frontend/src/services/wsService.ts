import type { InMsg, OutMsg } from "@/types/ws";

type Listener = (msg: OutMsg) => void;

class WSService {
    private ws?: WebSocket;
    private listeners: Listener[] = [];
    private pending: InMsg[] = [];

    baseURL = `${import.meta.env.VITE_TRELLO_LIKE_API_URL}` || 'http://localhost:9000'

    connect() {
        if (this.ws) return;

        const wsUrl = `${this.baseURL}/ws`;
        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
            console.log("[WS] Connected");
            // flush pending
            this.pending.forEach(msg => this.send(msg));
            this.pending = [];
            this.send({ type: "ping" });
        };

        this.ws.onmessage = (event) => {
            try {
                const msg: OutMsg = JSON.parse(event.data);
                console.log("[WS] Received:", msg);
                this.listeners.forEach((l) => l(msg));
            } catch (e) {
                console.error("[WS] Parse error:", e);
            }
        };

        this.ws.onclose = () => {
            console.warn("[WS] Closed, retrying...");
            this.ws = undefined;
            setTimeout(() => this.connect(), 3000);
        };
    }

    send(msg: InMsg) {
        if (this.ws?.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(msg));
        } else {
            console.log("[WS] Queued until open:", msg);
            this.pending.push(msg);
        }
    }

    onMessage(listener: Listener) {
        this.listeners.push(listener);
        return () => {
            this.listeners = this.listeners.filter((l) => l !== listener);
        };
    }
}

export const wsService = new WSService();
