import React from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Briefcase, Shield, Bell, Star, ArrowRight, IndianRupee } from 'lucide-react';
import { taskApi } from '../api/tasks';
import TaskCard from '../components/task/TaskCard';
import { useAuthStore } from '../store/authStore';

const INDIA_STATES = [
  'Andhra Pradesh', 'Delhi', 'Gujarat', 'Karnataka', 'Kerala',
  'Maharashtra', 'Rajasthan', 'Tamil Nadu', 'Telangana', 'Uttar Pradesh'
];

const HomePage: React.FC = () => {
  const { isAuthenticated } = useAuthStore();

  const { data: tasksData } = useQuery({
    queryKey: ['tasks', 'home'],
    queryFn: () => taskApi.getOpenTasks(undefined, 0, 6),
  });

  const tasks = tasksData?.data?.data?.content ?? [];

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-12">
      {/* Hero */}
      <div className="text-center space-y-4">
        <h1 className="text-4xl md:text-5xl font-bold text-gray-900">
          Find Work. Post Tasks.
          <span className="text-blue-600 block">Connect Across India.</span>
        </h1>
        <p className="text-lg text-gray-600 max-w-2xl mx-auto">
          Work Pool connects task publishers with skilled workers across India.
          From home repair to tutoring — get things done, earn money nearby.
        </p>
        <div className="flex flex-wrap justify-center gap-3 pt-2">
          <Link to="/tasks" className="btn-primary px-6 py-2.5 text-base">
            Browse Tasks <ArrowRight className="ml-1 w-4 h-4 inline" />
          </Link>
          {!isAuthenticated && (
            <Link to="/register" className="btn-secondary px-6 py-2.5 text-base">
              Join Free
            </Link>
          )}
          {isAuthenticated && (
            <Link to="/tasks/new" className="btn-secondary px-6 py-2.5 text-base">
              Post a Task
            </Link>
          )}
        </div>
      </div>

      {/* How it works */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {[
          { icon: <Briefcase className="w-7 h-7 text-blue-600" />, title: 'Post a Task', desc: 'Describe what you need — fixing, cleaning, teaching, shifting, and more.' },
          { icon: <Bell className="w-7 h-7 text-green-600" />, title: 'Get Matched', desc: 'Skilled workers nearby get notified when their skills match your task.' },
          { icon: <Shield className="w-7 h-7 text-purple-600" />, title: 'Secure Escrow', desc: 'Pay into secure escrow. Money releases only when the work is confirmed complete.' },
          { icon: <Star className="w-7 h-7 text-yellow-500" />, title: 'Rate & Trust', desc: 'Build a verifiable profile through ratings. More stars = more opportunities.' },
        ].map((step, i) => (
          <div key={i} className="card p-5 text-center space-y-2">
            <div className="flex justify-center">{step.icon}</div>
            <h3 className="font-semibold text-gray-900">{step.title}</h3>
            <p className="text-sm text-gray-600">{step.desc}</p>
          </div>
        ))}
      </div>

      {/* Commission callout */}
      <div className="bg-blue-50 rounded-xl p-6 flex flex-col sm:flex-row items-center gap-4">
        <IndianRupee className="w-10 h-10 text-blue-600 flex-shrink-0" />
        <div>
          <h3 className="font-semibold text-gray-900">Only 1% Commission — From Both Sides</h3>
          <p className="text-sm text-gray-600">
            Work Pool charges just 1% from the task publisher and 1% from the task finisher.
            That's the only platform fee. The rest is yours.
          </p>
        </div>
      </div>

      {/* Recent tasks */}
      {tasks.length > 0 && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold text-gray-900">Recent Open Tasks</h2>
            <Link to="/tasks" className="text-blue-600 text-sm font-medium hover:underline">View all →</Link>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {tasks.map((task) => <TaskCard key={task.id} task={task} />)}
          </div>
        </div>
      )}

      {/* Browse by State */}
      <div className="space-y-4">
        <h2 className="text-2xl font-bold text-gray-900">Browse by State</h2>
        <div className="flex flex-wrap gap-2">
          {INDIA_STATES.map((state) => (
            <Link key={state} to={`/tasks?state=${encodeURIComponent(state)}`}
              className="px-3 py-1.5 bg-white border border-gray-200 rounded-full text-sm text-gray-700 hover:bg-blue-50 hover:border-blue-300 transition-colors">
              {state}
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
};

export default HomePage;
