import { useNavigate } from 'react-router-dom';

const NotFound = () => {
    const navigate = useNavigate();

    return (
        <div className='min-h-screen flex items-center justify-center bg-gray-50'>
            <div className='text-center'>
                <div className='mb-4'>
                    <h1 className='text-6xl font-bold text-gray-900'>404</h1>
                </div>
                <h2 className='text-2xl font-semibold text-gray-900 mb-2'>
                    Page Not Found
                </h2>
                <p className='text-gray-600 mb-6'>
                    The page you're looking for doesn't exist or has been moved.
                </p>
                <div className='space-x-4'>
                    <button
                        onClick={() => navigate(-1)}
                        className='btn-secondary'
                    >
                        Go Back
                    </button>
                </div>
            </div>
        </div>
    );
};

export default NotFound;
