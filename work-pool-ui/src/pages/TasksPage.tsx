import React, { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { taskApi } from '../api/tasks';
import TaskCard from '../components/task/TaskCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { Search, SlidersHorizontal, ChevronLeft, ChevronRight } from 'lucide-react';

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
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-6">

      {/* Page title */}
      <div>
        <h1 className="section-title">Browse Tasks</h1>
        <p className="text-slate-500 text-sm mt-1">Find the right opportunity across India</p>
      </div>

      {/* Search + filter bar */}
      <div className="card-glass p-3 flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-brand-400" />
          <input
            className="input pl-10 bg-white/70"
            placeholder="Search tasks by title, skill, description…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="relative sm:w-56">
          <SlidersHorizontal className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-brand-400 pointer-events-none" />
          <select
            className="input pl-9 bg-white/70 appearance-none"
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
      </div>

      {/* Results */}
      {isLoading ? (
        <LoadingSpinner className="py-20" size="lg" />
      ) : filtered.length === 0 ? (
        <div className="text-center py-20 space-y-3">
          <div className="w-16 h-16 mx-auto rounded-full bg-brand-50 flex items-center justify-center">
            <Search className="w-8 h-8 text-brand-300" />
          </div>
          <p className="text-lg font-semibold text-slate-700">No tasks found</p>
          <p className="text-sm text-slate-400">Try adjusting your filters or check back later.</p>
        </div>
      ) : (
        <>
          <p className="text-sm text-slate-400 font-medium">
            <span className="text-brand-600 font-bold">{pageData?.totalElements ?? filtered.length}</span> tasks found
            {state && <> in <span className="text-slate-600 font-semibold">{state}</span></>}
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {filtered.map((task) => <TaskCard key={task.id} task={task} />)}
          </div>

          {/* Pagination */}
          {pageData && pageData.totalPages > 1 && (
            <div className="flex items-center justify-center gap-3 pt-4">
              <button
                disabled={pageData.first}
                onClick={() => setPage(page - 1)}
                className="btn-secondary text-sm px-3 py-2 gap-1 disabled:opacity-40"
              >
                <ChevronLeft className="w-4 h-4" /> Previous
              </button>
              <span className="text-sm font-semibold text-slate-600 bg-white/70 backdrop-blur-sm px-4 py-2 rounded-xl border border-brand-100">
                {page + 1} / {pageData.totalPages}
              </span>
              <button
                disabled={pageData.last}
                onClick={() => setPage(page + 1)}
                className="btn-secondary text-sm px-3 py-2 gap-1 disabled:opacity-40"
              >
                Next <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default TasksPage;
