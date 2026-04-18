import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { taskApi } from '../api/tasks';
import LoadingSpinner from '../components/common/LoadingSpinner';

const CATEGORIES = [
  'HOME_REPAIR', 'CLEANING', 'PLUMBING', 'ELECTRICAL', 'PAINTING', 'CARPENTRY',
  'GARDENING', 'TEACHING_TUTORING', 'COOKING', 'CHILDCARE', 'ELDER_CARE', 'PET_CARE',
  'MOVING_SHIFTING', 'DELIVERY', 'LAUNDRY', 'MARKETING_PROMOTION', 'BUSINESS_SUPPORT',
  'IT_TECH_SUPPORT', 'PHOTOGRAPHY_VIDEOGRAPHY', 'OTHER'
];

const INDIA_STATES = [
  'Andhra Pradesh', 'Assam', 'Bihar', 'Chhattisgarh', 'Delhi', 'Gujarat',
  'Haryana', 'Himachal Pradesh', 'Jharkhand', 'Karnataka', 'Kerala',
  'Madhya Pradesh', 'Maharashtra', 'Odisha', 'Punjab', 'Rajasthan',
  'Tamil Nadu', 'Telangana', 'Uttar Pradesh', 'West Bengal',
];

const SKILLS_SUGGESTIONS = [
  'Plumbing', 'Electrical Wiring', 'Carpentry', 'Painting', 'Cleaning',
  'Teaching', 'Cooking', 'Driving', 'Photography', 'Marketing', 'Coding',
];

const PostTaskPage: React.FC = () => {
  const navigate = useNavigate();
  const [skillInput, setSkillInput] = useState('');
  const [form, setForm] = useState({
    title: '', description: '', category: 'HOME_REPAIR',
    requiredSkills: [] as string[],
    city: '', district: '', state: '', pincode: '',
    budgetMin: '', budgetMax: '',
    scheduledStart: '', scheduledEnd: '',
  });
  const [error, setError] = useState('');

  const mutation = useMutation({
    mutationFn: () => taskApi.createTask({
      ...form,
      budgetMin: Number(form.budgetMin),
      budgetMax: Number(form.budgetMax),
      scheduledStart: form.scheduledStart || undefined,
      scheduledEnd: form.scheduledEnd || undefined,
    }),
    onSuccess: (res) => {
      navigate(`/tasks/${res.data.data?.id}`);
    },
    onError: (err: any) => setError(err.response?.data?.message || 'Failed to post task'),
  });

  const addSkill = (skill: string) => {
    const s = skill.trim();
    if (s && !form.requiredSkills.includes(s)) {
      setForm({ ...form, requiredSkills: [...form.requiredSkills, s] });
    }
    setSkillInput('');
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="card p-6 space-y-6">
        <h1 className="text-2xl font-bold text-gray-900">Post a Task</h1>

        {error && <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-3 py-2 rounded-lg">{error}</div>}

        <form onSubmit={(e) => { e.preventDefault(); mutation.mutate(); }} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Task Title *</label>
            <input className="input" type="text" placeholder="e.g. Fix leaking tap in bathroom"
              value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required minLength={5} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Category *</label>
            <select className="input" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
              {CATEGORIES.map((c) => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Description *</label>
            <textarea className="input" rows={4} placeholder="Describe the task in detail..."
              value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
              required minLength={20} />
          </div>

          {/* Skills */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Required Skills</label>
            <div className="flex gap-2 mb-2">
              <input className="input flex-1" type="text" placeholder="Add a skill..."
                value={skillInput} onChange={(e) => setSkillInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addSkill(skillInput); }}} />
              <button type="button" onClick={() => addSkill(skillInput)} className="btn-secondary text-sm px-3">Add</button>
            </div>
            <div className="flex flex-wrap gap-1 mb-2">
              {form.requiredSkills.map((s) => (
                <span key={s} className="bg-blue-100 text-blue-700 text-xs px-2 py-0.5 rounded-full flex items-center gap-1">
                  {s}
                  <button type="button" onClick={() => setForm({ ...form, requiredSkills: form.requiredSkills.filter((x) => x !== s) })} className="text-blue-500 hover:text-red-500">×</button>
                </span>
              ))}
            </div>
            <div className="flex flex-wrap gap-1">
              {SKILLS_SUGGESTIONS.filter((s) => !form.requiredSkills.includes(s)).map((s) => (
                <button type="button" key={s} onClick={() => addSkill(s)}
                  className="text-xs px-2 py-0.5 border border-gray-200 rounded-full text-gray-600 hover:bg-gray-50">+ {s}</button>
              ))}
            </div>
          </div>

          {/* Location */}
          <fieldset className="space-y-3">
            <legend className="text-sm font-medium text-gray-700">Location *</legend>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs text-gray-500 mb-1">City</label>
                <input className="input" placeholder="e.g. Hyderabad"
                  value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} required />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">District</label>
                <input className="input" placeholder="e.g. Hyderabad District"
                  value={form.district} onChange={(e) => setForm({ ...form, district: e.target.value })} required />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">State</label>
                <select className="input" value={form.state} onChange={(e) => setForm({ ...form, state: e.target.value })} required>
                  <option value="">Select state</option>
                  {INDIA_STATES.map((s) => <option key={s} value={s}>{s}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Pincode</label>
                <input className="input" placeholder="500001"
                  value={form.pincode} onChange={(e) => setForm({ ...form, pincode: e.target.value })} />
              </div>
            </div>
          </fieldset>

          {/* Budget */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Min Budget (₹) *</label>
              <input className="input" type="number" min={100} placeholder="e.g. 500"
                value={form.budgetMin} onChange={(e) => setForm({ ...form, budgetMin: e.target.value })} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Max Budget (₹) *</label>
              <input className="input" type="number" min={100} placeholder="e.g. 2000"
                value={form.budgetMax} onChange={(e) => setForm({ ...form, budgetMax: e.target.value })} required />
            </div>
          </div>

          {/* Schedule */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Start Date (optional)</label>
              <input className="input" type="datetime-local"
                value={form.scheduledStart} onChange={(e) => setForm({ ...form, scheduledStart: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">End Date (optional)</label>
              <input className="input" type="datetime-local"
                value={form.scheduledEnd} onChange={(e) => setForm({ ...form, scheduledEnd: e.target.value })} />
            </div>
          </div>

          <button type="submit" disabled={mutation.isPending} className="btn-primary w-full py-3 text-base">
            {mutation.isPending ? <LoadingSpinner size="sm" className="inline" /> : '🚀 Post Task'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default PostTaskPage;
