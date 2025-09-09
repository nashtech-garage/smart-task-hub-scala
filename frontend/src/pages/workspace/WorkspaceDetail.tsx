import { Card } from '@/components/ui/card';
import { updateWorkspace } from '@/services/workspaceService';
import { Pencil, Users } from 'lucide-react';
import { useState, type SetStateAction } from 'react';

// Mock data for workspace and projects
const workspace = {
  id: 2,
  name: 'NashTech',
  description: 'A workspace for NashTech projects'
};
const projects = [
  { id: 1, name: "Project 1", image: "https://assets.isu.pub/document-structure/230202140706-d8f20619598ed7e219152cf8a970792f/v1/99d245c3778f2e1fe872924cb0bda7fd.jpeg" },
  { id: 2, name: "Project 2", image: "https://assets.isu.pub/document-structure/230202140706-d8f20619598ed7e219152cf8a970792f/v1/99d245c3778f2e1fe872924cb0bda7fd.jpeg" },
  { id: 3, name: "project 3", image: "https://assets.isu.pub/document-structure/230202140706-d8f20619598ed7e219152cf8a970792f/v1/99d245c3778f2e1fe872924cb0bda7fd.jpeg" },
  { id: 4, name: "Project 4", image: "https://assets.isu.pub/document-structure/230202140706-d8f20619598ed7e219152cf8a970792f/v1/99d245c3778f2e1fe872924cb0bda7fd.jpeg" },
  { id: 5, name: "Project 5", image: "https://assets.isu.pub/document-structure/230202140706-d8f20619598ed7e219152cf8a970792f/v1/99d245c3778f2e1fe872924cb0bda7fd.jpeg" },
]

const WorkspaceDetail = () => {
  const [workspaceName, setWorkspaceName] = useState('NashTech');
  const [workspaceDescription, setWorkspaceDescription] = useState('A workspace for NashTech projects');
  const [workspaceNameError, setWorkspaceNameError] = useState('');
  const [workspaceDescriptionError, setWorkspaceDescriptionError] = useState('');
  const [isEditingWorkspace, setIsEditingWorkspace] = useState(false);

  const onEditingButtonClick = () => {
    setIsEditingWorkspace(!isEditingWorkspace);
    setWorkspaceName(workspace.name);
    setWorkspaceDescription(workspace.description);
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    // Handle form submission logic here
    console.log('Workspace updated:', { name: workspaceName, description: workspaceDescription });
    try {
      await updateWorkspace(
        workspace.id,
        workspaceName,
        workspaceDescription || null
      );
      setIsEditingWorkspace(false);
    } catch (error: any) {
      const errorFields = error?.response?.data?.errors || [];
      // Set error messages based on the response
      errorFields.forEach((element: { field: string; message: SetStateAction<string>; }) => {
        switch (element.field) {
          case 'name':
            setWorkspaceNameError(element.message);
            break;
          case 'description':
            setWorkspaceDescriptionError(element.message);
            break;
        }
      });
    }

  }

  return (
    <div className='max-w-4xl mx-auto'>
      {/* Workspace header section */}
      <div className='my-11'>
        {isEditingWorkspace ? (
          <div className='mt-4 ml-6'>
            <form>
              <div className='mb-2'>
                <label className='block text-sm text-gray-400 mb-1 font-bold' htmlFor='workspaceName'>Name <span className='text-red-500'>*</span></label>
                <div className='text-red-500 text-sm mb-1'>
                  {workspaceNameError}
                </div>
                <input
                  type='text'
                  defaultValue={workspaceName}
                  id='workspaceName'
                  name='name'
                  onChange={(e) => setWorkspaceName(e.target.value)}
                  placeholder='Update workspace name...'
                  className='min-w-1/2 p-2 rounded border-1 border-neutral-500 text-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent'
                />
              </div>
              <div className='mb-2'>
                <label className='block text-sm text-gray-400 mb-1 font-bold' htmlFor='workspaceDescription'>Description</label>
                <div className='text-red-500 text-sm mb-1'>
                  {workspaceDescriptionError}
                </div>
                <textarea
                  rows={3}
                  defaultValue={workspaceDescription}
                  id='workspaceDescription'
                  name='description'
                  onChange={(e) => setWorkspaceDescription(e.target.value)}
                  placeholder='Update workspace name...'
                  className='min-w-1/2 p-2 rounded border-1 border-neutral-500 text-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent'
                />
              </div>
              <button
                type='submit'
                onClick={handleSubmit}
                className='px-4 py-2 bg-blue-500 text-gray-800 font-semibold rounded hover:bg-blue-500 transition-colors'
              >
                Save
              </button>
              <button
                type='button'
                className='ml-2 px-4 py-2 bg-gray-700 font-semibold text-gray-400 rounded hover:bg-gray-600 transition-colors'
                onClick={() => onEditingButtonClick()}
              >
                Cancel
              </button>
            </form>
          </div>
        ) : <div>
          <div className='flex items-center ml-6'>
            <div className='w-15 h-15 bg-orange-500 rounded flex items-center justify-center text-black font-bold text-4xl mr-3'>
              {workspace.name.charAt(0)}
            </div>
            <span className='text-xl font-medium text-gray-400'>
              {workspace.name}
            </span>
            <button className='ml-3 p-2 rounded hover:bg-gray-600 transition-colors' onClick={() => setIsEditingWorkspace(true)}>
              <Pencil className='w-4 h-4 text-gray-400' />
            </button>
          </div>
          <div className='ml-6 mt-2 text-gray-400'>
            <p className='text-sm'>{workspace.description}</p>
          </div>
        </div>}
        {/* Workspace name and icon */}


      </div>
      <hr className='my-4 border-gray-600' />

      {/* Your boards section */}
      <div>
        <div className="flex items-center gap-2 mb-6">
          <Users className="w-5 h-5 text-gray-400" />
          <h2 className="text-lg font-medium text-gray-400">Your boards</h2>
        </div>
        {/* Boards grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 xl:grid-cols-4 gap-6">
          {projects.map((project) => (
            <Card
              key={project.id}
              className="bg-slate-800 border-slate-700 hover:bg-slate-750 transition-colors cursor-pointer group"
            >
              <div className="aspect-video relative overflow-hidden rounded-t-lg">
                <img
                  src={project.image || "https://assets.isu.pub/document-structure/230202140706-d8f20619598ed7e219152cf8a970792f/v1/99d245c3778f2e1fe872924cb0bda7fd.jpeg"}
                  alt={project.name}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
                />
              </div>
              <div className="ml-2 p-2">
                <h3 className="text-sm font-medium text-gray-400">{project.name}</h3>
              </div>
            </Card>
          ))}

          {/* Create new board card */}
          <Card className="bg-slate-800 border-slate-700 hover:bg-slate-750 transition-colors cursor-pointer border-dashed">
            <div className="aspect-video flex flex-col items-center justify-center text-center p-6">
              <div className="text-gray-400 mb-2">
                <div className="w-8 h-8 border-2 border-dashed border-gray-500 rounded mx-auto mb-3"></div>
              </div>
              <h3 className="text-sm font-medium text-gray-300 mb-1">Create new board</h3>
              <p className="text-xs text-gray-500">5 remaining</p>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
export default WorkspaceDetail;