import React, { useState, useEffect, useRef } from "react";

const WebSocketURL = "ws://localhost:8083/hits";

const Canvas = () => {
    const canvasRef = useRef(null);
    const [dots, setDots] = useState([]);

    useEffect(() => {
        const ws = new WebSocket(WebSocketURL);

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);

                const newDot = {
                    x: (data.ix / 240) * 800,  // Масштабирование X (0-240 → 0-800)
                    y: (data.iy / 240) * 800,  // Масштабирование Y (0-240 → 0-800)
                    id: Date.now(),
                    color: data.isSignal ? "red" : "grey"
                };

                setDots((prevDots) => [...prevDots, newDot]);

                setTimeout(() => {
                    setDots((prevDots) => prevDots.filter(dot => dot.id !== newDot.id));
                }, 500);
            } catch (error) {
                console.error("Error parsing WebSocket message:", error);
            }
        };

        return () => {
            ws.close();
        };
    }, []);

    useEffect(() => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext("2d");

        ctx.clearRect(0, 0, canvas.width, canvas.height); // Очищаем холст

        dots.forEach(({ x, y, color }) => {
            ctx.fillStyle = color;
            ctx.beginPath();
            ctx.arc(x, y, 5, 0, 2 * Math.PI);
            ctx.fill();
        });

    }, [dots]);

    return (
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", background: "#1a1a1a", color: "white", height: "100vh", justifyContent: "center" }}>
            <h1 style={{ color: "white", textAlign: "center", margin: "0px 0px 20px" }}>Hit Visualization</h1>
            <canvas ref={canvasRef} width={800} height={800} style={{ border: "2px solid white" }} />
        </div>
    );
};

export default Canvas;
