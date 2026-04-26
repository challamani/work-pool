import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { taskApi } from '../api/tasks';
import LoadingSpinner from '../components/common/LoadingSpinner';
import type { ApiResponse } from '../types';
import { MapPin, IndianRupee, CalendarDays, Tag, AlignLeft, Rocket } from 'lucide-react';

const CATEGORIES = [
  'HOME_REPAIR', 'CLEANING', 'PLUMBING', 'ELECTRICAL', 'PAINTING', 'CARPENTRY',
  'GARDENING', 'TEACHING_TUTORING', 'COOKING', 'CHILDCARE', 'ELDER_CARE', 'PET_CARE',
  'MOVING_SHIFTING', 'DELIVERY', 'LAUNDRY', 'MARKETING_PROMOTION', 'BUSINESS_SUPPORT',
  'IT_TECH_SUPPORT', 'PHOTOGRAPHY_VIDEOGRAPHY', 'OTHER',
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

const SectionHeader: React.FC<{ icon: React.ElementType; title: string }> = ({ icon: Icon, title }) => (
  <div className="flex items-center gap-2 mb-4">
    <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-brand-500 to-indigo-500 flex items-center justify-center">
      <Icon className="w-4 h-4 text-white" />
    </div>
    <h2 className="font-bold text-slate-800 text-sm uppercase tracking-wide">{title}</h2>
  </div>
);

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
    onError: (err) => {
      const apiError = err as AxiosError<ApiResponse<null>>;
      setError(apiError.response?.data?.message || 'Failed to post task');
    },
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
      {/* Page header */}
      <div className="mb-6">
        <h1 className="section-title">Post a Task</h1>
        <p className="text-slate-500 text-sm mt-1">Fill in the details and get matched with skilled workers near you.</p>
      </div>

      {error && (
        <div className="mb-5 bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">{error}</div>
      )}

      <form onSubmit={(e) => { e.preventDefault(); mutation.mutate(); }} className="space-y-6">

        {/* Basic info */}
        <div className="card p-6">
          <SectionHeader icon={AlignLeft} title="Task Details" />
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1.5">Task Title *</label>
              <input className="input" type="text" placeholder="e.g. Fix leaking tap in bathroom"
                value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required minLength={5} />
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1.5">Category *</label>
              <select className="input" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
                {CATEGORIES.map((c) => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1.5">Description *</label>
              <textarea className="input" rows={4} placeholder="Describe the task in detail — the more info, the better matches you'll get."
                value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
                required minLength={20} />
            </div>
          </div>
        </div>

        {/* Skills */}
        <div className="card p-6">
          <SectionHeader icon={Tag} title="Required Skills" />
          <div className="space-y-3">
            <div className="flex gap-2">
              <input className="input flex-1" type="text" placeholder="Add a required skill…"
                value={skillInput} onChange={(e) => setSkillInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addSkill(skillInput); }}} />
              <button type="button" onClick={() => addSkill(skillInput)} className="btn-secondary text-sm px-4">Add</button>
            </div>
            {form.requiredSkills.length > 0 && (
              <div className="flex flex-wrap gap-1.5">
                {form.requiredSkills.map((s) => (
                  <span key={s} className="bg-brand-100 text-brand-700 text-xs px-3 py-1 rounded-full font-semibold flex items-center gap-1.5 border border-brand-200">
                    {s}
                    <button type="button" onClick={() => setForm({ ...form, requiredSkills: form.requiredSkills.filter((x) => x !== s) })}
                      className="text-brand-400 hover:text-red-500 font-bold">×</button>
                  </span>
                ))}
              </div>
            )}
            <div className="flex flex-wrap gap-1.5 pt-1">
              {SKILLS_SUGGESTIONS.filter((s) => !form.requiredSkills.includes(s)).map((s) => (
                <button type="button" key={s} onClick={() => addSkill(s)}
                  className="text-xs px-2.5 py-1 border border-slate-200 rounded-full text-slate-600 hover:bg-brand-50 hover:border-brand-200 hover:text-brand-700 transition-colors">
                  + {s}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Location */}
        <div className="card p-6">
          <SectionHeader icon={MapPin} title="Location" />
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-500 mb-1">City *</label>
              <input className="input" placeholder="e.g. Hyderabad"
                value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} required />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-500 mb-1">District *</label>
              <input className="input" placeholder="e.g. Hyderabad District"
                value={form.district} onChange={(e) => setForm({ ...form, district: e.target.value })} required />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-500 mb-1">State *</label>
              <select className="input" value={form.state} onChange={(e) => setForm({ ...form, state: e.target.value })} required>
                <option value="">Select state</option>
                {INDIA_STATES.map((s) => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-500 mb-1">Pincode</label>
              <input className="input" placeholder="500001"
                value={form.pincode} onChange={(e) => setForm({ ...form, pincode: e.target.value })} />
            </div>
          </div>
        </div>

        {/* Budget */}
        <div className="card p-6">
          <SectionHeader icon={IndianRupee} title="Budget Range" />
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1.5">Min Budget (₹) *</label>
              <input className="input" type="number" min={100} placeholder="e.g. 500"
                value={form.budgetMin} onChange={(e) => setForm({ ...form, budgetMin: e.target.value })} required />
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1.5">Max Budget (₹) *</label>
              <input className="input" type="number" min={100} placeholder="e.g. 2000"
                value={form.budgetMax} onChange={(e) => setForm({ ...form, budgetMax: e.target.value })} required />
            </div>
          </div>
        </div>

        {/* Schedule */}
        <div className="card p-6">
          <SectionHeader icon={CalendarDays} title="Schedule (Optional)" />
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1.5">Start Date</label>
              <input className="input" type="datetime-local"
                value={form.scheduledStart} onChange={(e) => setForm({ ...form, scheduledStart: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1.5">End Date</label>
              <input className="input" type="datetime-local"
                value={form.scheduledEnd} onChange={(e) => setForm({ ...form, scheduledEnd: e.target.value })} />
            </div>
          </div>
        </div>

        <button type="submit" disabled={mutation.isPending} className="btn-primary w-full py-3.5 text-base gap-2 shadow-brand-lg">
          {mutation.isPending ? <LoadingSpinner size="sm" className="inline" /> : (
            <><Rocket className="w-5 h-5" /> Post Task</>
          )}
        </button>
      </form>
    </div>
  );
};

export default PostTaskPage;
