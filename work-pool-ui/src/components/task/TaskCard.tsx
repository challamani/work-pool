import React from 'react';
import { Link } from 'react-router-dom';
import { MapPin, Clock, IndianRupee } from 'lucide-react';
import type { Task, TaskStatus } from '../../types';

interface TaskCardProps {
  task: Task;
}

const statusColors: Record<TaskStatus, string> = {
  DRAFT: 'bg-gray-100 text-gray-700',
  OPEN: 'bg-green-100 text-green-700',
  BIDDING: 'bg-blue-100 text-blue-700',
  ASSIGNED: 'bg-purple-100 text-purple-700',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-700',
  PENDING_REVIEW: 'bg-orange-100 text-orange-700',
  COMPLETED: 'bg-gray-100 text-gray-600',
  CANCELLED: 'bg-red-100 text-red-700',
  DISPUTED: 'bg-red-200 text-red-800',
};

const categoryLabels: Record<string, string> = {
  HOME_REPAIR: '🔧 Home Repair',
  CLEANING: '🧹 Cleaning',
  PLUMBING: '🚿 Plumbing',
  ELECTRICAL: '⚡ Electrical',
  PAINTING: '🎨 Painting',
  CARPENTRY: '🪵 Carpentry',
  GARDENING: '🌱 Gardening',
  TEACHING_TUTORING: '📚 Teaching',
  COOKING: '🍳 Cooking',
  CHILDCARE: '👶 Childcare',
  ELDER_CARE: '👴 Elder Care',
  PET_CARE: '🐾 Pet Care',
  MOVING_SHIFTING: '📦 Moving',
  DELIVERY: '🚚 Delivery',
  LAUNDRY: '👕 Laundry',
  MARKETING_PROMOTION: '📢 Marketing',
  BUSINESS_SUPPORT: '💼 Business Support',
  IT_TECH_SUPPORT: '💻 IT Support',
  PHOTOGRAPHY_VIDEOGRAPHY: '📸 Photography',
  OTHER: '✨ Other',
};

const TaskCard: React.FC<TaskCardProps> = ({ task }) => {
  return (
    <Link to={`/tasks/${task.id}`} className="card p-4 flex flex-col gap-3 hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between gap-2">
        <div className="flex-1 min-w-0">
          <h3 className="font-semibold text-gray-900 text-sm leading-snug line-clamp-2">{task.title}</h3>
          <span className="text-xs text-gray-500 mt-0.5 block">{categoryLabels[task.category] || task.category}</span>
        </div>
        <span className={`badge whitespace-nowrap ${statusColors[task.status]}`}>{task.status}</span>
      </div>

      <p className="text-sm text-gray-600 line-clamp-2">{task.description}</p>

      <div className="flex flex-wrap gap-2 text-xs text-gray-500">
        {task.location && (
          <span className="flex items-center gap-1">
            <MapPin className="w-3 h-3" />
            {task.location.city}, {task.location.state}
          </span>
        )}
        <span className="flex items-center gap-1">
          <IndianRupee className="w-3 h-3" />
          ₹{task.budgetMin.toLocaleString('en-IN')} – ₹{task.budgetMax.toLocaleString('en-IN')}
        </span>
        {task.bidCount > 0 && (
          <span className="text-blue-600">{task.bidCount} bid{task.bidCount !== 1 ? 's' : ''}</span>
        )}
      </div>

      {task.requiredSkills?.length > 0 && (
        <div className="flex flex-wrap gap-1">
          {task.requiredSkills.slice(0, 3).map((s) => (
            <span key={s} className="bg-gray-100 text-gray-600 text-xs px-2 py-0.5 rounded-full">{s}</span>
          ))}
          {task.requiredSkills.length > 3 && (
            <span className="text-xs text-gray-400">+{task.requiredSkills.length - 3} more</span>
          )}
        </div>
      )}

      <div className="flex items-center gap-1 text-xs text-gray-400 mt-auto">
        <Clock className="w-3 h-3" />
        {new Date(task.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}
      </div>
    </Link>
  );
};

export default TaskCard;
