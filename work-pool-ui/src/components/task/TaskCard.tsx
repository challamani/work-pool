import React from 'react';
import { Link } from 'react-router-dom';
import { MapPin, Clock, IndianRupee } from 'lucide-react';
import type { Task, TaskStatus } from '../../types';

interface TaskCardProps {
  task: Task;
}

const statusColors: Record<TaskStatus, string> = {
  DRAFT:          'bg-slate-100 text-slate-600',
  OPEN:           'bg-emerald-100 text-emerald-700',
  BIDDING:        'bg-brand-100 text-brand-700',
  ASSIGNED:       'bg-violet-100 text-violet-700',
  IN_PROGRESS:    'bg-amber-100 text-amber-700',
  PENDING_REVIEW: 'bg-orange-100 text-orange-700',
  COMPLETED:      'bg-slate-100 text-slate-500',
  CANCELLED:      'bg-red-100 text-red-600',
  DISPUTED:       'bg-red-200 text-red-800',
};

const statusDot: Record<TaskStatus, string> = {
  DRAFT:          'bg-slate-400',
  OPEN:           'bg-emerald-500',
  BIDDING:        'bg-brand-500',
  ASSIGNED:       'bg-violet-500',
  IN_PROGRESS:    'bg-amber-500',
  PENDING_REVIEW: 'bg-orange-500',
  COMPLETED:      'bg-slate-400',
  CANCELLED:      'bg-red-500',
  DISPUTED:       'bg-red-700',
};

const categoryLabels: Record<string, string> = {
  HOME_REPAIR:           '🔧 Home Repair',
  CLEANING:              '🧹 Cleaning',
  PLUMBING:              '🚿 Plumbing',
  ELECTRICAL:            '⚡ Electrical',
  PAINTING:              '🎨 Painting',
  CARPENTRY:             '🪵 Carpentry',
  GARDENING:             '🌱 Gardening',
  TEACHING_TUTORING:     '📚 Teaching',
  COOKING:               '🍳 Cooking',
  CHILDCARE:             '👶 Childcare',
  ELDER_CARE:            '👴 Elder Care',
  PET_CARE:              '🐾 Pet Care',
  MOVING_SHIFTING:       '📦 Moving',
  DELIVERY:              '🚚 Delivery',
  LAUNDRY:               '👕 Laundry',
  MARKETING_PROMOTION:   '📢 Marketing',
  BUSINESS_SUPPORT:      '💼 Business',
  IT_TECH_SUPPORT:       '💻 IT Support',
  PHOTOGRAPHY_VIDEOGRAPHY:'📸 Photography',
  OTHER:                 '✨ Other',
};

const TaskCard: React.FC<TaskCardProps> = ({ task }) => {
  return (
    <Link
      to={`/tasks/${task.id}`}
      className="relative card flex flex-col gap-3 p-5 group
                 hover:shadow-card-hover hover:-translate-y-1 transition-all duration-300
                 border border-white/80 overflow-hidden"
    >
      {/* Coloured top accent bar */}
      <div className={`absolute top-0 left-0 right-0 h-0.5 ${statusDot[task.status]} opacity-60`} />

      {/* Header row */}
      <div className="flex items-start justify-between gap-2">
        <div className="flex-1 min-w-0">
          <h3 className="font-bold text-slate-900 text-sm leading-snug line-clamp-2 group-hover:text-brand-700 transition-colors">
            {task.title}
          </h3>
          <span className="text-xs text-slate-400 mt-0.5 block font-medium">{categoryLabels[task.category] || task.category}</span>
        </div>
        <span className={`badge whitespace-nowrap text-[10px] font-bold ${statusColors[task.status]}`}>
          <span className={`inline-block w-1.5 h-1.5 rounded-full mr-1 ${statusDot[task.status]}`} />
          {task.status}
        </span>
      </div>

      <p className="text-sm text-slate-500 line-clamp-2 leading-relaxed">{task.description}</p>

      {/* Meta */}
      <div className="flex flex-wrap gap-2 text-xs text-slate-400">
        {task.location && (
          <span className="flex items-center gap-1 bg-slate-50 px-2 py-0.5 rounded-full border border-slate-100">
            <MapPin className="w-3 h-3 text-ocean-500" />
            {task.location.city}, {task.location.state}
          </span>
        )}
        <span className="flex items-center gap-1 bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded-full border border-emerald-100 font-semibold">
          <IndianRupee className="w-3 h-3" />
          ₹{task.budgetMin.toLocaleString('en-IN')} – ₹{task.budgetMax.toLocaleString('en-IN')}
        </span>
        {task.bidCount > 0 && (
          <span className="text-brand-600 font-semibold bg-brand-50 px-2 py-0.5 rounded-full border border-brand-100">
            {task.bidCount} bid{task.bidCount !== 1 ? 's' : ''}
          </span>
        )}
      </div>

      {task.requiredSkills?.length > 0 && (
        <div className="flex flex-wrap gap-1">
          {task.requiredSkills.slice(0, 3).map((s) => (
            <span key={s} className="bg-brand-50 text-brand-600 text-xs px-2 py-0.5 rounded-full border border-brand-100 font-medium">{s}</span>
          ))}
          {task.requiredSkills.length > 3 && (
            <span className="text-xs text-slate-400">+{task.requiredSkills.length - 3} more</span>
          )}
        </div>
      )}

      <div className="flex items-center gap-1 text-xs text-slate-300 mt-auto">
        <Clock className="w-3 h-3" />
        {new Date(task.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}
      </div>
    </Link>
  );
};

export default TaskCard;
