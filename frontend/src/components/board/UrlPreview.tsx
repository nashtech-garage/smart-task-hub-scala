import { useState, useEffect, useRef } from 'react';
import { ExternalLink, X } from 'lucide-react';
import type { UrlPreviewData } from '@/types';
import { fetchUrlPreviewUtil } from '@/utils/UrlPreviewUtils';

interface UrlPreviewProps {
    url: string;
    onRemove?: () => void;
    isDragging?: boolean;
    showRemoveButton?: boolean;
}

const UrlPreview: React.FC<UrlPreviewProps> = ({
    url,
    onRemove,
    isDragging = false,
    showRemoveButton = false,
}) => {
    const [previewData, setPreviewData] = useState<UrlPreviewData | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(false);

    // Track previous dragging state to detect when drag ends
    const hasFetchedRef = useRef(false);
    const fetchedUrlRef = useRef<string>('');
    const isInitialMountRef = useRef(true);

    useEffect(() => {
        const isUrlChanged = fetchedUrlRef.current !== url;
        const shouldInitialFetch =
            isInitialMountRef.current && !hasFetchedRef.current;

        if ((isUrlChanged || shouldInitialFetch) && !isDragging) {
            fetchUrlPreview(url);
        }

        isInitialMountRef.current = false;
    }, [url, isDragging]);

    // Reset states only when URL actually changes (not on reorder)
    useEffect(() => {
        if (fetchedUrlRef.current !== url && fetchedUrlRef.current !== '') {
            // URL has genuinely changed, reset everything
            setPreviewData(null);
            setError(false);
            setIsLoading(false);
            hasFetchedRef.current = false;
        }
    }, [url]);

    const fetchUrlPreview = async (targetUrl: string) => {
        // Don't fetch if we already have data for this URL
        if (
            fetchedUrlRef.current === targetUrl &&
            hasFetchedRef.current &&
            previewData
        ) {
            return;
        }

        if (isDragging) {
            return;
        }

        try {
            setIsLoading(true);
            setError(false);

            const preview = await fetchUrlPreviewUtil(targetUrl);
            setPreviewData(preview);
        } catch (err) {
            setError(true);
            console.error('Failed to fetch URL preview:', err);
        } finally {
            setIsLoading(false);
        }
    };

    // Show simplified view while dragging (if preview data exists)
    if (isDragging && previewData) {
        return (
            <div className='border border-[#394B59] rounded-lg overflow-hidden bg-[#22272B] opacity-90'>
                <div className='p-3'>
                    <h3 className='text-[#B6C2CF] font-medium text-sm mb-1 line-clamp-1'>
                        {previewData.title || 'Link Preview'}
                    </h3>
                    <div className='flex items-center gap-1 text-xs text-blue-400'>
                        <ExternalLink size={12} />
                        <span className='truncate'>
                            {new URL(url).hostname}
                        </span>
                    </div>
                </div>
            </div>
        );
    }

    // Show minimal view while dragging (if no preview data yet)
    if (isDragging) {
        return (
            <div className='border border-[#394B59] rounded-lg p-3 bg-[#22272B] opacity-90'>
                <div className='flex items-center gap-2 text-[#B6C2CF]'>
                    <ExternalLink size={16} />
                    <span className='text-sm truncate'>{url}</span>
                </div>
            </div>
        );
    }

    // Loading state (only when not dragging)
    if (isLoading && !previewData) {
        return (
            <div className='border border-[#394B59] rounded-lg p-3 bg-[#22272B] animate-pulse'>
                <div className='flex gap-3'>
                    <div className='w-16 h-16 bg-[#394B59] rounded'></div>
                    <div className='flex-1 space-y-2'>
                        <div className='h-4 bg-[#394B59] rounded w-3/4'></div>
                        <div className='h-3 bg-[#394B59] rounded w-full'></div>
                        <div className='h-3 bg-[#394B59] rounded w-2/3'></div>
                    </div>
                </div>
            </div>
        );
    }

    if ((error || !previewData) && !isLoading) {
        return (
            <div className='border border-[#394B59] rounded-lg p-3 bg-[#22272B] relative'>
                {showRemoveButton && (
                    <button
                        onClick={onRemove}
                        className='absolute top-2 right-2 text-gray-400 hover:text-gray-300'
                    >
                        <X size={16} />
                    </button>
                )}
                <div className='flex items-center gap-2 text-[#B6C2CF]'>
                    <ExternalLink size={16} />
                    <span className='text-sm truncate'>{url}</span>
                </div>
            </div>
        );
    }

    if (previewData) {
        return (
            <div className='border border-[#394B59] rounded-lg overflow-hidden bg-[#22272B] hover:border-[#4A90E2] transition-colors cursor-pointer relative group'>
                {showRemoveButton && (
                    <button
                        onClick={e => {
                            e.stopPropagation();
                            onRemove?.();
                        }}
                        className='absolute top-2 right-2 z-10 text-gray-400 hover:text-gray-300 opacity-0 group-hover:opacity-100 transition-opacity bg-black bg-opacity-50 rounded p-1'
                    >
                        <X size={14} />
                    </button>
                )}

                <a
                    href={url}
                    target='_blank'
                    rel='noopener noreferrer'
                    className='block'
                >
                    {previewData.image && (
                        <div className='aspect-video w-full overflow-hidden'>
                            <img
                                src={previewData.image}
                                alt={previewData.title}
                                className='w-full h-full object-cover'
                                onError={e => {
                                    (
                                        e.target as HTMLImageElement
                                    ).style.display = 'none';
                                }}
                            />
                        </div>
                    )}

                    <div className='p-3'>
                        {previewData.siteName && (
                            <div className='flex items-center gap-2 text-xs text-gray-400 mb-2'>
                                {previewData.favicon && (
                                    <img
                                        src={previewData.favicon}
                                        alt=''
                                        className='w-4 h-4'
                                    />
                                )}
                                <span>{previewData.siteName}</span>
                            </div>
                        )}

                        {previewData.title && (
                            <h3 className='text-[#B6C2CF] font-medium text-sm mb-1 line-clamp-2'>
                                {previewData.title}
                            </h3>
                        )}

                        {previewData.description && (
                            <p className='text-gray-400 text-xs line-clamp-2'>
                                {previewData.description}
                            </p>
                        )}

                        <div className='flex items-center gap-1 text-xs text-blue-400 mt-2'>
                            <ExternalLink size={12} />
                            <span className='truncate'>
                                {new URL(url).hostname}
                            </span>
                        </div>
                    </div>
                </a>
            </div>
        );
    }

    return null;
};

export default UrlPreview;
