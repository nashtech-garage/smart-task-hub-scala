const LoadingContent = () => {
    return (
        <div className="flex items-center justify-center">
            <style>{`
                @keyframes wave {
                    0%, 40%, 100% {
                        transform: scaleY(0.4);
                    }
                    20% {
                        transform: scaleY(1);
                    }
                }
                .wave-bar {
                    animation: wave 1.2s infinite ease-in-out;
                }
                .wave-bar:nth-child(1) {
                    animation-delay: -1.1s;
                }
                .wave-bar:nth-child(2) {
                    animation-delay: -1.0s;
                }
                .wave-bar:nth-child(3) {
                    animation-delay: -0.9s;
                }
            `}</style>
            <div className="flex items-end space-x-1">
                <div className="w-1 h-6 bg-gray-400 rounded-sm wave-bar"></div>
                <div className="w-1 h-6 bg-gray-400 rounded-sm wave-bar"></div>
                <div className="w-1 h-6 bg-gray-400 rounded-sm wave-bar"></div>
            </div>
        </div>
    );
};

export default LoadingContent;