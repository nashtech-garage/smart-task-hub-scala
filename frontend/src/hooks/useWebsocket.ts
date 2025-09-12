import { wsService } from "@/services/wsService";
import type { InMsg, OutMsg } from "@/types/ws";
import { useEffect, useState } from "react";

export function useWebSocket() {
  const [lastMessage, setLastMessage] = useState<OutMsg | null>(null);

  useEffect(() => {
    wsService.connect();

    const unsubscribe = wsService.onMessage((msg) => {
      setLastMessage(msg);
    });

    return () => unsubscribe();
  }, []);

  const send = (msg: InMsg) => wsService.send(msg);

  return { lastMessage, send };
}