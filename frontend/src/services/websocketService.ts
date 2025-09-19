// websocketService.ts
let socket: WebSocket | null = null;

export function connectToProjectWS(projectId: number, onMessage: (msg: any) => void) {
  const url = `ws://localhost:9000/ws/project/${projectId}`;
  socket = new WebSocket(url);

  socket.onopen = () => {
    console.log("✅ WebSocket connected:", url);
  };

  socket.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data);
      onMessage(message);
    } catch (err) {
      console.error("❌ Error parsing WS message:", err);
    }
  };

  socket.onclose = () => {
    console.log("⚠️ WebSocket closed");
  };

  socket.onerror = (err) => {
    console.error("❌ WebSocket error:", err);
  };

  return socket;
}

export function disconnectWS() {
  if (socket) {
    socket.close();
    socket = null;
  }
}

export function sendMessage(message: any) {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify(message));
  }
}
