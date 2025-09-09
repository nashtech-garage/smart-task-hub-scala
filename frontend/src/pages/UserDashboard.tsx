import React, { useState } from 'react';

interface Template {
  id: string;
  title: string;
  category: string;
  image: string;
  color: string;
}

interface Board {
  id: string;
  title: string;
  color: string;
}

const UserDashboard: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = useState('all');

  const templates: Template[] = [
    {
      id: '1',
      title: 'Basic Board',
      category: 'project-management',
      image: '/api/placeholder/300/200',
      color: 'bg-blue-500'
    },
    {
      id: '2',
      title: 'Kanban Template',
      category: 'project-management',
      image: '/api/placeholder/300/200',
      color: 'bg-gradient-to-br from-pink-400 to-blue-400'
    },
    {
      id: '3',
      title: 'Daily Task Management Template | Trello',
      category: 'productivity',
      image: '/api/placeholder/300/200',
      color: 'bg-gradient-to-br from-gray-600 to-gray-800'
    },
    {
      id: '4',
      title: 'Remote Team Hub',
      category: 'remote-work',
      image: '/api/placeholder/300/200',
      color: 'bg-gradient-to-br from-yellow-400 to-orange-500'
    }
  ];

  const recentBoards: Board[] = [
    {
      id: '1',
      title: 'Daily Task Management Template | Trello',
      color: 'bg-gradient-to-br from-gray-600 to-gray-800'
    },
    {
      id: '2',
      title: 'Smart TaskHub',
      color: 'bg-blue-600'
    }
  ];

  const workspaceBoards: Board[] = [
    {
      id: '1',
      title: 'Smart TaskHub',
      color: 'bg-blue-600'
    }
  ];

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <div className="w-64 bg-white border-r border-gray-200 flex flex-col">
        <div className="p-4">
          {/* Navigation Items */}
          <nav className="space-y-1">
            <a href="#" className="flex items-center px-3 py-2 text-sm font-medium text-blue-600 bg-blue-50 rounded-lg">
              <svg className="w-5 h-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
              Boards
            </a>
            <a href="#" className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg">
              <svg className="w-5 h-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z" />
              </svg>
              Templates
            </a>
            <a href="#" className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg">
              <svg className="w-5 h-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
              Home
            </a>
          </nav>
        </div>

        {/* Workspaces Section */}
        <div className="flex-1 px-4">
          <div className="mb-4">
            <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">Workspaces</h3>
            <div className="space-y-1">
              <div className="flex items-center justify-between p-2 hover:bg-gray-100 rounded-lg cursor-pointer">
                <div className="flex items-center">
                  <div className="w-8 h-8 bg-orange-500 rounded flex items-center justify-center text-white font-semibold text-sm mr-3">
                    N
                  </div>
                  <span className="text-sm font-medium text-gray-900">Nashtech</span>
                </div>
                <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </div>

              {/* Workspace submenu */}
              <div className="ml-11 space-y-1">
                <a href="#" className="block px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100 rounded">
                  <svg className="w-4 h-4 inline mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                  </svg>
                  Boards
                </a>
                <a href="#" className="block px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100 rounded">
                  <svg className="w-4 h-4 inline mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
                  </svg>
                  Members
                  <svg className="w-3 h-3 inline ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                </a>
                <a href="#" className="block px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100 rounded">
                  <svg className="w-4 h-4 inline mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                  Settings
                </a>
              </div>
            </div>
          </div>

          {/* Try Trello Premium */}
          <div className="bg-gradient-to-r from-blue-500 to-purple-600 rounded-lg p-4 text-white">
            <h4 className="font-semibold text-sm mb-1">Try Trello Premium</h4>
            <p className="text-xs opacity-90 mb-2">Get Planner (full access), Atlassian Intelligence, card mirroring, list colors, and more.</p>
            <button className="text-xs underline hover:no-underline">Start free trial</button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 overflow-auto">
        <div className="p-6">
          {/* Most Popular Templates */}
          <div className="mb-8">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900">Most popular templates</h2>
              <button className="text-sm text-gray-500 hover:text-gray-700">×</button>
            </div>
            <p className="text-sm text-gray-600 mb-4">
              Get going faster with a template from the Trello community or{' '}
              <select 
                className="border border-gray-300 rounded px-2 py-1 text-sm"
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
              >
                <option value="all">choose a category</option>
                <option value="project-management">Project Management</option>
                <option value="productivity">Productivity</option>
                <option value="remote-work">Remote Work</option>
              </select>
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              {templates.map((template) => (
                <div key={template.id} className="group shadow-md rounded-lg cursor-pointer">
                  <div className={`${template.color} rounded-lg h-32 relative overflow-hidden`}>
                    <div className="absolute top-2 left-2">
                      <span className="bg-yellow-400 text-yellow-900 text-xs font-medium px-2 py-0.5 rounded">
                        TEMPLATE
                      </span>
                    </div>
                    <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all duration-200"></div>
                  </div>
                  <h3 className="mt-2 text-sm font-medium text-gray-900 group-hover:text-blue-600 p-2">
                    {template.title}
                  </h3>
                </div>
              ))}
            </div>

            <button className="mt-4 text-sm text-blue-600 hover:text-blue-700 hover:underline">
              Browse the full template gallery
            </button>
          </div>

          {/* Recently Viewed */}
          <div className="mb-8">
            <div className="flex items-center mb-4">
              <svg className="w-5 h-5 mr-2 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <h2 className="text-lg font-semibold text-gray-900">Recently viewed</h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              {recentBoards.map((board) => (
                <div key={board.id} className="group cursor-pointer">
                  <div className={`${board.color} rounded-lg h-24 relative overflow-hidden`}>
                    {board.title.includes('Template') && (
                      <div className="absolute top-2 left-2">
                        <span className="bg-yellow-400 text-yellow-900 text-xs font-medium px-2 py-0.5 rounded">
                          TEMPLATE
                        </span>
                      </div>
                    )}
                    <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all duration-200"></div>
                  </div>
                  <h3 className="mt-2 text-sm font-medium text-gray-900 group-hover:text-blue-600">
                    {board.title}
                  </h3>
                </div>
              ))}
            </div>
          </div>

          {/* Your Workspaces */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
              YOUR WORKSPACES
            </h2>

            {/* Nashtech Workspace */}
            <div className="mb-6">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center">
                  <div className="w-8 h-8 bg-orange-500 rounded flex items-center justify-center text-white font-semibold text-sm mr-3">
                    N
                  </div>
                  <h3 className="text-lg font-medium text-gray-900">Nashtech</h3>
                </div>
                <div className="flex items-center space-x-2">
                  <button className="flex items-center px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100 rounded">
                    <svg className="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
                    </svg>
                    Members
                  </button>
                  <button className="flex items-center px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100 rounded">
                    <svg className="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    Settings
                  </button>
                  <button className="flex items-center px-3 py-1.5 text-sm text-blue-600 hover:bg-blue-50 rounded">
                    <svg className="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                    </svg>
                    Upgrade
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {workspaceBoards.map((board) => (
                  <div key={board.id} className="group cursor-pointer">
                    <div className={`${board.color} rounded-lg h-24 relative overflow-hidden`}>
                      <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all duration-200"></div>
                    </div>
                    <h3 className="mt-2 text-sm font-medium text-gray-900 group-hover:text-blue-600">
                      {board.title}
                    </h3>
                  </div>
                ))}
                
                {/* Create new board card */}
                <div className="group cursor-pointer">
                  <div className="bg-gray-200 hover:bg-gray-300 rounded-lg h-24 flex items-center justify-center transition-colors duration-200">
                    <div className="text-center">
                      <div className="text-gray-600 group-hover:text-gray-700">
                        <svg className="w-8 h-8 mx-auto mb-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                        </svg>
                      </div>
                    </div>
                  </div>
                  <h3 className="mt-2 text-sm font-medium text-gray-600 group-hover:text-gray-800">
                    Create new board
                  </h3>
                </div>
              </div>

              <button className="mt-4 text-sm text-gray-600 hover:text-gray-800 hover:underline">
                View all closed boards
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserDashboard;