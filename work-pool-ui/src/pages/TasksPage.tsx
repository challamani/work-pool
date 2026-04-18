import React, { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { taskApi } from '../api/tasks';
import TaskCard from '../components/task/TaskCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { Search } from 'lucide-react';

const INDIA_STATES = [
  '', 'Andhra Pradesh', 'Assam', 'Bihar', 'Chhattisgarh', 'Delhi',
  'Gujarat', 'Haryana', 'Himachal Pradesh', 'Jharkhand', 'Karnataka',
  'Kerala', 'Madhya Pradesh', 'Maharashtra', 'Odisha', 'Punjab',
  'Rajasthan', 'Tamil Nadu', 'Telangana', 'Uttar Pradesh', 'West Bengal',
];

const TasksPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);

  const state = searchParams.get('state') || '';

  const { data, isLoading } = useQuery({
    queryKey: ['tasks', state, page],
    queryFn: () => taskApi.getOpenTasks(state || undefined, page),
  });

  const tasks = data?.data?.data?.content ?? [];
  const pageData = data?.data?.data;

  const filtered = search
    ? tasks.filter((t) =>
        t.title.toLowerCase().includes(search.toLowerCase()) ||
        t.description.toLowerCase().includes(search.toLowerCase()) ||
        t.requiredSkills?.some((s) => s.toLowerCase().includes(search.toLowerCase()))
      )
    : tasks;

  return (
    <div className="max-w-7xl mx-auto px-4 py-6 space-y-6">
      <div className="flex flex-col sm:flex-row gap-3">
        {/* Search */}
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            className="input pl-9"
            placeholder="Search tasks by title, skill..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        {/* State filter */}
        <select
          className="input sm:w-56"
          value={state}
          onChange={(e) => {
            setSearchParams(e.target.value ? { state: e.target.value } : {});
            setPage(0);
          }}
        >
          {INDIA_STATES.map((s) => (
            <option key={s} value={s}>{s || 'All States'}</option>
          ))}
        </select>
      </div>

      {isLoading ? (
        <LoadingSpinner className="py-12" />
      ) : filtered.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          <p className="text-lg font-medium">No tasks found</p>
          <p className="text-sm">Try adjusting your filters or check back later.</p>
        </div>
      ) : (
        <>
          <p className="text-sm text-gray-500">{pageData?.totalElements ?? filtered.length} tasks found</p>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {filtered.map((task) => <TaskCard key={task.id} task={task} />)}
          </div>

          {/* Pagination */}
          {pageData && pageData.totalPages > 1 && (
            <div className="flex justify-center gap-2 pt-4">
              <button
                disabled={pageData.first}
                onClick={() => setPage(page - 1)}
                className="btn-secondary text-sm px-3 py-1.5 disabled:opacity-40"
              >
                Previous
              </button>
              <span className="text-sm text-gray-600 py-1.5">
                Page {page + 1} of {pageData.totalPages}
              </span>
              <button
                disabled={pageData.last}
                onClick={() => setPage(page + 1)}
                className="btn-secondary text-sm px-3 py-1.5 disabled:opacity-40"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default TasksPage;
