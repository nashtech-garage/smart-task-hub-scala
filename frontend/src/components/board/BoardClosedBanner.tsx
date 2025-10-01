import { Lock, Unlock } from "lucide-react";
import type React from "react";

interface BoardClosedBannerProps {
    isBoardClosed: boolean;
    handleReopenBoard: () => void;
}

const BoardClosedBanner: React.FC<BoardClosedBannerProps> = ({ isBoardClosed, handleReopenBoard }) => {
    return (
        <>
            {isBoardClosed ? (
                <div className='bg-red-500 text-white px-4 py-3 flex items-center justify-between'>
                    <div className='flex items-center gap-2'>
                        <Lock size={18} />
                        <span className='font-medium'>
                            This board is closed. Reopen the board to make changes.
                        </span>
                    </div>
                    <button
                        onClick={handleReopenBoard}
                        className='bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md flex items-center gap-2 transition-colors font-medium'
                    >
                        <Unlock size={16} />
                        Reopen board
                    </button>
                </div>
            ) : null}
        </>
    )
}

export default BoardClosedBanner