import React from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  Briefcase, Shield, Bell, Star, ArrowRight, IndianRupee,
  Zap, MapPin, TrendingUp, Users,
} from 'lucide-react';
import { taskApi } from '../api/tasks';
import TaskCard from '../components/task/TaskCard';
import { useAuthStore } from '../store/authStore';

const INDIA_STATES = [
  'Andhra Pradesh', 'Delhi', 'Gujarat', 'Karnataka', 'Kerala',
  'Maharashtra', 'Rajasthan', 'Tamil Nadu', 'Telangana', 'Uttar Pradesh',
];

const STATS = [
  { value: '50K+', label: 'Tasks Posted',    icon: Briefcase  },
  { value: '120K+', label: 'Workers Ready',  icon: Users      },
  { value: '₹2Cr+', label: 'Paid Out',       icon: TrendingUp },
  { value: '4.8★',  label: 'Avg Rating',     icon: Star       },
];

const HOW_IT_WORKS = [
  {
    icon: Briefcase,
    color: 'from-brand-500 to-indigo-500',
    bg: 'bg-brand-50',
    title: 'Post a Task',
    desc: 'Describe what you need — fixing, cleaning, teaching, shifting, and more.',
  },
  {
    icon: Bell,
    color: 'from-ocean-500 to-cyan-400',
    bg: 'bg-cyan-50',
    title: 'Get Matched',
    desc: 'Skilled workers nearby get notified when their skills match your task.',
  },
  {
    icon: Shield,
    color: 'from-violet-500 to-purple-400',
    bg: 'bg-violet-50',
    title: 'Secure Escrow',
    desc: 'Pay into secure escrow. Money releases only when work is confirmed.',
  },
  {
    icon: Star,
    color: 'from-flame-500 to-amber-400',
    bg: 'bg-amber-50',
    title: 'Rate & Trust',
    desc: 'Build a verifiable profile through ratings. More stars = more opportunities.',
  },
];

const HomePage: React.FC = () => {
  const { isAuthenticated } = useAuthStore();

  const { data: tasksData } = useQuery({
    queryKey: ['tasks', 'home'],
    queryFn: () => taskApi.getOpenTasks(undefined, 0, 6),
  });

  const tasks = tasksData?.data?.data?.content ?? [];

  return (
    <div className="space-y-16 pb-8">

      {/* ── Hero ───────────────────────────────────────────────── */}
      <section className="relative overflow-hidden pt-14 pb-20 px-4">
        <div className="relative z-10 max-w-4xl mx-auto text-center space-y-6 animate-fade-in">
          <span className="inline-flex items-center gap-1.5 bg-brand-100/80 backdrop-blur-sm text-brand-700 text-xs font-semibold px-3.5 py-1.5 rounded-full border border-brand-200 shadow-sm">
            <Zap className="w-3.5 h-3.5 fill-brand-500" />
            India's fastest-growing gig marketplace
          </span>

          <h1 className="text-4xl sm:text-5xl md:text-6xl font-extrabold leading-tight text-slate-900">
            Find Work.{' '}
            <span className="text-gradient">Post Tasks.</span>
            <br className="hidden sm:block" />
            Connect Across{' '}
            <span className="relative inline-block">
              India
              <svg className="absolute -bottom-1 left-0 w-full" viewBox="0 0 200 8" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M2 6 Q50 2, 100 5 Q150 8, 198 4" stroke="#7c3aed" strokeWidth="3" strokeLinecap="round" fill="none"/>
              </svg>
            </span>
            .
          </h1>

          <p className="text-lg text-slate-600 max-w-2xl mx-auto leading-relaxed">
            Work Pool connects task publishers with skilled workers across India.
            From home repair to tutoring — get things done, earn money nearby.
          </p>

          <div className="flex flex-wrap justify-center gap-3 pt-2">
            <Link to="/tasks" className="btn-primary px-7 py-3 text-base gap-2 shadow-brand-lg">
              Browse Tasks
              <ArrowRight className="w-4 h-4" />
            </Link>
            {!isAuthenticated && (
              <Link to="/register" className="btn-secondary px-7 py-3 text-base">
                Join Free ✨
              </Link>
            )}
            {isAuthenticated && (
              <Link to="/tasks/new" className="btn-secondary px-7 py-3 text-base">
                Post a Task
              </Link>
            )}
          </div>
        </div>

        {/* Floating stat pills */}
        <div className="relative z-10 max-w-3xl mx-auto mt-12 grid grid-cols-2 sm:grid-cols-4 gap-3 px-4">
          {STATS.map(({ value, label, icon: Icon }) => (
            <div key={label} className="card-glass text-center p-4 space-y-1 animate-slide-up">
              <Icon className="w-5 h-5 text-brand-500 mx-auto" />
              <p className="text-xl font-extrabold text-slate-900">{value}</p>
              <p className="text-xs text-slate-500 font-medium">{label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── How it works ───────────────────────────────────────── */}
      <section className="max-w-7xl mx-auto px-4 space-y-8">
        <div className="text-center space-y-2">
          <h2 className="section-title">How it works</h2>
          <p className="text-slate-500 text-sm">Four simple steps to get started</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {HOW_IT_WORKS.map((step, i) => (
            <div key={i} className="card p-6 space-y-4 group hover:shadow-card-hover hover:-translate-y-1 transition-all duration-300">
              <div className={`w-12 h-12 rounded-2xl bg-gradient-to-br ${step.color} flex items-center justify-center shadow-brand group-hover:scale-110 transition-transform`}>
                <step.icon className="w-6 h-6 text-white" />
              </div>
              <div className="space-y-1">
                <span className="text-xs font-bold text-brand-400 uppercase tracking-wider">Step {i + 1}</span>
                <h3 className="font-bold text-slate-900">{step.title}</h3>
                <p className="text-sm text-slate-500 leading-relaxed">{step.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── Commission callout ─────────────────────────────────── */}
      <section className="max-w-7xl mx-auto px-4">
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-brand-600 via-indigo-600 to-ocean-600 p-8 sm:p-10">
          {/* decorative circles */}
          <div className="absolute top-0 right-0 w-64 h-64 rounded-full bg-white/10 -translate-y-1/2 translate-x-1/4" />
          <div className="absolute bottom-0 left-16 w-32 h-32 rounded-full bg-white/5 translate-y-1/2" />

          <div className="relative z-10 flex flex-col sm:flex-row items-center sm:items-start gap-5">
            <div className="w-14 h-14 rounded-2xl bg-white/20 flex items-center justify-center flex-shrink-0 backdrop-blur-sm">
              <IndianRupee className="w-7 h-7 text-white" />
            </div>
            <div className="text-center sm:text-left">
              <h3 className="text-xl font-bold text-white">Only 1% Commission — From Both Sides</h3>
              <p className="mt-1 text-brand-200 text-sm leading-relaxed max-w-lg">
                Work Pool charges just 1% from the task publisher and 1% from the task finisher.
                That's the only platform fee. The rest is yours to keep.
              </p>
              <Link to="/register" className="inline-flex items-center gap-1.5 mt-4 bg-white text-brand-700 font-semibold text-sm px-5 py-2 rounded-xl hover:bg-brand-50 transition-colors shadow-sm">
                Start Earning <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* ── Recent tasks ───────────────────────────────────────── */}
      {tasks.length > 0 && (
        <section className="max-w-7xl mx-auto px-4 space-y-5">
          <div className="flex items-center justify-between">
            <h2 className="section-title">Recent Open Tasks</h2>
            <Link to="/tasks" className="text-brand-600 text-sm font-semibold hover:text-brand-700 flex items-center gap-1">
              View all <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {tasks.map((task) => <TaskCard key={task.id} task={task} />)}
          </div>
        </section>
      )}

      {/* ── Browse by State ────────────────────────────────────── */}
      <section className="max-w-7xl mx-auto px-4 space-y-5">
        <div className="flex items-center gap-2">
          <MapPin className="w-5 h-5 text-brand-500" />
          <h2 className="section-title">Browse by State</h2>
        </div>
        <div className="flex flex-wrap gap-2">
          {INDIA_STATES.map((state) => (
            <Link
              key={state}
              to={`/tasks?state=${encodeURIComponent(state)}`}
              className="px-4 py-2 bg-white/70 backdrop-blur-sm border border-brand-100 rounded-full text-sm font-medium text-slate-700
                         hover:bg-brand-50 hover:border-brand-300 hover:text-brand-700 hover:shadow-sm transition-all duration-200"
            >
              {state}
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
};

export default HomePage;
