const UnauthorizedFallback = () => (
    <div className='flex items-center justify-center min-h-screen'>
        <div className='text-center'>
            <div className='mb-4'>
                <svg
                    className='mx-auto h-12 w-12 text-gray-400'
                    fill='none'
                    viewBox='0 0 24 24'
                    stroke='currentColor'
                >
                    <path
                        strokeLinecap='round'
                        strokeLinejoin='round'
                        strokeWidth={2}
                        d='M12 15v2m0 0v2m0-2h2m-2 0H10m2-10V7m0 0V5m0 2H10m2 0h2m-4 8a7 7 0 100-14 7 7 0 000 14z'
                    />
                </svg>
            </div>
            <h2 className='text-2xl font-semibold text-gray-900 mb-2'>
                Access Denied
            </h2>
            <p className='text-gray-600 mb-4'>
                You don't have permission to view this page.
            </p>
            <button
                onClick={() => window.history.back()}
                className='btn-secondary'
            >
                Go Back
            </button>
        </div>
    </div>
);

export default UnauthorizedFallback;
